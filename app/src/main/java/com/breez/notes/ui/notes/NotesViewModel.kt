package com.breez.notes.ui.notes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.R
import com.breez.notes.data.backup.NotesBackupStore
import com.breez.notes.domain.model.ChecklistFormat
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.usecase.DeleteNote
import com.breez.notes.domain.usecase.SaveNote
import com.breez.notes.ocr.ImageTextRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val searchQuery: String = "",
    val selectedFolderId: Long? = null,
    val showingArchive: Boolean = false,
    val isEmpty: Boolean = true,
    val searchingImage: Boolean = false,
    val searchMessage: String? = null,
    val backupMessage: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val deleteNoteUseCase: DeleteNote,
    private val saveNote: SaveNote,
    private val backupStore: NotesBackupStore,
    folderRepository: FolderRepository,
    private val imageTextRecognizer: ImageTextRecognizer,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFolderId = MutableStateFlow<Long?>(null)
    private val showingArchive = MutableStateFlow(false)
    private val searchingImage = MutableStateFlow(false)
    private val searchMessage = MutableStateFlow<String?>(null)
    private val backupMessage = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notesFlow = combine(searchQuery, selectedFolderId, showingArchive) { query, folderId, archive ->
        Triple(query, folderId, archive)
    }.flatMapLatest { (query, folderId, archive) ->
        when {
            archive -> noteRepository.observeArchived()
            query.isNotBlank() -> noteRepository.observeSearch(query.trim())
            folderId != null -> noteRepository.observeByFolder(folderId)
            else -> noteRepository.observeAll()
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        combine(notesFlow, folderRepository.observeAll(), searchQuery, selectedFolderId, showingArchive) {
                notes, folders, query, folderId, archive ->
            NotesUiState(
                notes = notes,
                folders = folders,
                searchQuery = query,
                selectedFolderId = folderId,
                showingArchive = archive,
                isEmpty = notes.isEmpty()
            )
        },
        combine(searchingImage, searchMessage, backupMessage) { imageSearch, message, backup ->
            Triple(imageSearch, message, backup)
        }
    ) { state, extra ->
        state.copy(
            searchingImage = extra.first,
            searchMessage = extra.second,
            backupMessage = extra.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onSearchChange(query: String) {
        searchQuery.value = query
        searchMessage.value = null
    }

    fun selectFolder(folderId: Long?) {
        showingArchive.value = false
        selectedFolderId.value = folderId
    }

    fun showArchive() {
        showingArchive.value = true
        selectedFolderId.value = null
        searchQuery.value = ""
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { deleteNoteUseCase(note) }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch { noteRepository.setArchived(note.id, true) }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch { noteRepository.setArchived(note.id, false) }
    }

    fun swipeNote(note: Note) {
        if (showingArchive.value) deleteNote(note) else archiveNote(note)
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { noteRepository.setPinned(note.id, !note.isPinned) }
    }

    fun toggleChecklistItem(note: Note, visibleIndex: Int) {
        viewModelScope.launch {
            val updated = note.copy(body = ChecklistFormat.toggleVisible(note.body, visibleIndex))
            saveNote(updated, allowEmpty = true)
        }
    }

    fun reorderNotes(notes: List<Note>) {
        viewModelScope.launch { noteRepository.reorder(notes) }
    }

    fun renameNote(note: Note, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { noteRepository.rename(note.id, title) }
    }

    fun moveNote(note: Note, folderId: Long?) {
        viewModelScope.launch { noteRepository.moveToFolder(note.id, folderId) }
    }

    fun copyNote(note: Note, folderId: Long?) {
        viewModelScope.launch { noteRepository.copyToFolder(note.id, folderId) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupStore.exportTo(uri) }
                .onSuccess { backupMessage.value = context.getString(R.string.backup_exported) }
                .onFailure { backupMessage.value = context.getString(R.string.backup_failed) }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupStore.importFrom(uri) }
                .onSuccess { count ->
                    backupMessage.value = context.getString(R.string.backup_imported, count)
                }
                .onFailure { backupMessage.value = context.getString(R.string.backup_failed) }
        }
    }

    fun clearBackupMessage() {
        backupMessage.value = null
    }

    fun searchByImage(uri: Uri) {
        viewModelScope.launch {
            searchingImage.value = true
            searchMessage.value = null
            val text = runCatching { imageTextRecognizer.fromUri(uri) }.getOrDefault("")
            searchingImage.value = false
            if (text.isBlank()) {
                searchMessage.value = context.getString(R.string.search_image_empty)
                return@launch
            }
            val query = text.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.length >= 3 }
                ?: text.take(80)
            searchQuery.value = query
            searchMessage.value = context.getString(R.string.search_image_found, query)
        }
    }
}
