package com.breez.notes.ui.notes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.usecase.DeleteNote
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
    val isEmpty: Boolean = true,
    val searchingImage: Boolean = false,
    val searchMessage: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val deleteNote: DeleteNote,
    folderRepository: FolderRepository,
    private val imageTextRecognizer: ImageTextRecognizer,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFolderId = MutableStateFlow<Long?>(null)
    private val searchingImage = MutableStateFlow(false)
    private val searchMessage = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val notesFlow = combine(searchQuery, selectedFolderId) { query, folderId ->
        query to folderId
    }.flatMapLatest { (query, folderId) ->
        when {
            query.isNotBlank() -> noteRepository.observeSearch(query.trim())
            folderId != null -> noteRepository.observeByFolder(folderId)
            else -> noteRepository.observeAll()
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        combine(
            notesFlow,
            folderRepository.observeAll(),
            searchQuery,
            selectedFolderId,
            searchingImage
        ) { notes, folders, query, folderId, imageSearch ->
            NotesUiState(
                notes = notes,
                folders = folders,
                searchQuery = query,
                selectedFolderId = folderId,
                isEmpty = notes.isEmpty(),
                searchingImage = imageSearch
            )
        },
        searchMessage
    ) { state, message ->
        state.copy(searchMessage = message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onSearchChange(query: String) {
        searchQuery.value = query
        searchMessage.value = null
    }

    fun selectFolder(folderId: Long?) {
        selectedFolderId.value = folderId
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { deleteNote(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { noteRepository.setPinned(note.id, !note.isPinned) }
    }

    fun reorderNotes(notes: List<Note>) {
        viewModelScope.launch { noteRepository.reorder(notes) }
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
