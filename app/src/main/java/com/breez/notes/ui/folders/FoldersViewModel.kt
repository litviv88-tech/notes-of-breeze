package com.breez.notes.ui.folders

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.FolderMarkSaveResult
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val folders: StateFlow<List<Folder>> = folderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _markError = MutableStateFlow<String?>(null)
    val markError: StateFlow<String?> = _markError.asStateFlow()

    fun clearMarkError() {
        _markError.value = null
    }

    fun createFolder(
        name: String,
        colorHex: String,
        markType: FolderMarkType,
        mediaUri: Uri?,
        mimeType: String?,
        onDone: () -> Unit
    ) {
        saveFolder(
            folder = Folder(name = name.trim(), colorHex = colorHex, markType = markType),
            mediaUri = mediaUri,
            mimeType = mimeType,
            onDone = onDone
        )
    }

    fun updateFolder(
        folder: Folder,
        name: String,
        colorHex: String,
        markType: FolderMarkType,
        mediaUri: Uri?,
        mimeType: String?,
        onDone: () -> Unit
    ) {
        saveFolder(
            folder = folder.copy(
                name = name.trim(),
                colorHex = colorHex,
                markType = markType,
                markFileName = if (markType == folder.markType && mediaUri == null) folder.markFileName else ""
            ),
            mediaUri = mediaUri,
            mimeType = mimeType,
            onDone = onDone
        )
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch { folderRepository.delete(folder) }
    }

    fun reorderFolders(folders: List<Folder>) {
        viewModelScope.launch { folderRepository.reorder(folders) }
    }

    fun moveNote(noteId: Long, folderId: Long?) {
        viewModelScope.launch { noteRepository.moveToFolder(noteId, folderId) }
    }

    fun notesInFolder(folderId: Long): StateFlow<List<Note>> =
        noteRepository.observeByFolder(folderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun saveFolder(
        folder: Folder,
        mediaUri: Uri?,
        mimeType: String?,
        onDone: () -> Unit
    ) {
        if (folder.name.isBlank()) return
        viewModelScope.launch {
            when (folderRepository.saveWithMark(folder, mediaUri, mimeType)) {
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
}
