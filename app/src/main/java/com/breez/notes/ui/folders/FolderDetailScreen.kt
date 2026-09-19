package com.breez.notes.ui.folders

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.FolderMarkSaveResult
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.usecase.DeleteNote
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.notes.FolderTargetDialog
import com.breez.notes.ui.notes.NoteOrganizeAction
import com.breez.notes.ui.notes.RenameTitleDialog
import com.breez.notes.ui.reorder.ReorderableNoteList
import com.breez.notes.ui.theme.Transparent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderDetailUiState(
    val folder: Folder? = null,
    val notes: List<Note> = emptyList(),
    val allFolders: List<Folder> = emptyList()
)

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val deleteNoteUseCase: DeleteNote,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val folderId: Long = savedStateHandle.get<Long>("folderId") ?: -1L

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FolderDetailUiState> = folderRepository.observeAll()
        .flatMapLatest { folders ->
            val folder = folders.firstOrNull { it.id == folderId }
            if (folder == null) {
                flowOf(FolderDetailUiState(allFolders = folders))
            } else {
                noteRepository.observeByFolder(folderId).let { notesFlow ->
                    kotlinx.coroutines.flow.combine(notesFlow, flowOf(folders)) { notes, all ->
                        FolderDetailUiState(folder = folder, notes = notes, allFolders = all)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderDetailUiState())

    private val _markError = MutableStateFlow<String?>(null)
    val markError: StateFlow<String?> = _markError.asStateFlow()

    fun clearMarkError() {
        _markError.value = null
    }

    fun save(
        name: String,
        colorHex: String,
        markType: FolderMarkType,
        mediaUri: Uri?,
        mimeType: String?,
        onDone: () -> Unit
    ) {
        val folder = uiState.value.folder ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val updated = folder.copy(
                name = name.trim(),
                colorHex = colorHex,
                markType = markType,
                markFileName = if (markType == folder.markType && mediaUri == null) folder.markFileName else ""
            )
            when (folderRepository.saveWithMark(updated, mediaUri, mimeType)) {
                is FolderMarkSaveResult.Ok -> {
                    _markError.value = null
                    onDone()
                }
                FolderMarkSaveResult.VideoTooLong -> {
                    _markError.value = context.getString(R.string.folder_video_too_long)
                }
                FolderMarkSaveResult.Failed -> {
                    _markError.value = context.getString(R.string.folder_mark_failed)
                }
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        val folder = uiState.value.folder ?: return
        viewModelScope.launch {
            folderRepository.delete(folder)
            onDone()
        }
    }

    fun moveNote(noteId: Long, targetFolderId: Long?) {
        viewModelScope.launch { noteRepository.moveToFolder(noteId, targetFolderId) }
    }

    fun copyNote(noteId: Long, targetFolderId: Long?) {
        viewModelScope.launch { noteRepository.copyToFolder(noteId, targetFolderId) }
    }

    fun renameNote(noteId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { noteRepository.rename(noteId, title) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { noteRepository.setArchived(note.id, true) }
    }

    fun toggleChecklistItem(note: Note, visibleIndex: Int) {
        viewModelScope.launch {
            noteRepository.upsert(
                note.copy(body = com.breez.notes.domain.model.ChecklistFormat.toggleVisible(note.body, visibleIndex))
            )
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { noteRepository.setPinned(note.id, !note.isPinned) }
    }

    fun reorderNotes(notes: List<Note>) {
        viewModelScope.launch { noteRepository.reorder(notes) }
    }
}

@Composable
fun FolderDetailScreen(
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val markError by viewModel.markError.collectAsStateWithLifecycle()
    val folder = state.folder
    val context = LocalContext.current
    var editOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    var organizeNote by remember { mutableStateOf<Note?>(null) }
    var organizeAction by remember { mutableStateOf<NoteOrganizeAction?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = {
            BreezTopBar(
                title = folder?.name ?: stringResource(R.string.folders_title),
                onBack = onBack,
                transparent = true,
                actions = {
                    IconButton(onClick = { editOpen = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.folder_rename))
                    }
                    IconButton(onClick = { deleteOpen = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.folder_delete))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (folder != null) {
                FolderMarkCover(
                    folder = folder,
                    playVideo = folder.markType == FolderMarkType.VIDEO,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable(enabled = folder.hasMediaMark) {
                            openFolderMark(context, folder)
                        }
                )
            }
            if (state.notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.notes_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                ReorderableNoteList(
                    notes = state.notes,
                    enabled = true,
                    onReorder = viewModel::reorderNotes,
                    onOpenNote = onOpenNote,
                    onDelete = viewModel::deleteNote,
                    onTogglePin = viewModel::togglePin,
                    onToggleChecklistItem = viewModel::toggleChecklistItem,
                    onOrganize = { note, action ->
                        organizeNote = note
                        organizeAction = action
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    FolderEditDialog(
        visible = editOpen && folder != null,
        existing = folder,
        error = markError,
        onClearError = viewModel::clearMarkError,
        onDismiss = { editOpen = false },
        onSave = { name, colorHex, markType, mediaUri, mimeType ->
            viewModel.save(name, colorHex, markType, mediaUri, mimeType) {
                editOpen = false
            }
        }
    )

    if (deleteOpen) {
        AlertDialog(
            onDismissRequest = { deleteOpen = false },
            confirmButton = {
                BreezButton(
                    text = stringResource(R.string.folder_delete),
                    onClick = { viewModel.delete(onBack) }
                )
            },
            dismissButton = {
                BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = { deleteOpen = false })
            },
            title = { Text(stringResource(R.string.folder_delete_confirm)) }
        )
    }

    val currentNote = organizeNote
    RenameTitleDialog(
        visible = currentNote != null && organizeAction == NoteOrganizeAction.RENAME,
        title = stringResource(R.string.note_rename),
        initial = currentNote?.title.orEmpty(),
        hint = stringResource(R.string.note_rename_hint),
        onDismiss = {
            organizeNote = null
            organizeAction = null
        },
        onConfirm = { title ->
            currentNote?.let { viewModel.renameNote(it.id, title) }
            organizeNote = null
            organizeAction = null
        }
    )
    FolderTargetDialog(
        visible = currentNote != null &&
            (organizeAction == NoteOrganizeAction.COPY || organizeAction == NoteOrganizeAction.MOVE),
        title = stringResource(
            if (organizeAction == NoteOrganizeAction.COPY) {
                R.string.note_copy_to_folder
            } else {
                R.string.note_move_to_folder
            }
        ),
        folders = state.allFolders,
        selectedId = currentNote?.folderId ?: folder?.id,
        onDismiss = {
            organizeNote = null
            organizeAction = null
        },
        onConfirm = { folderId ->
            val note = currentNote
            val action = organizeAction
            if (note != null) {
                if (action == NoteOrganizeAction.COPY) viewModel.copyNote(note.id, folderId)
                else viewModel.moveNote(note.id, folderId)
            }
            organizeNote = null
            organizeAction = null
        }
    )
}
