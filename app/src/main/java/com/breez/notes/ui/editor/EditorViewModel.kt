package com.breez.notes.ui.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.domain.model.ChecklistFormat
import com.breez.notes.domain.model.ChecklistItem
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.NoteAttachment
import com.breez.notes.domain.model.Recurrence
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.usecase.DeleteNote
import com.breez.notes.domain.usecase.SaveNote
import com.breez.notes.reminders.MeetingPlaceLocator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val body: String = "",
    val folderId: Long? = null,
    val colorHex: String = Note.DEFAULT_NOTE_COLOR,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val reminderAt: Long? = null,
    val meetingPlace: String = "",
    val meetingLat: Double? = null,
    val meetingLng: Double? = null,
    val locationReminder: Boolean = false,
    val recurrence: Recurrence = Recurrence(),
    val attachments: List<NoteAttachment> = emptyList(),
    val isChecklist: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val folders: List<Folder> = emptyList(),
    val isNew: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val saveNote: SaveNote,
    private val deleteNote: DeleteNote,
    folderRepository: FolderRepository,
    private val meetingPlaceLocator: MeetingPlaceLocator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val incomingId: Long = savedStateHandle.get<Long>("noteId") ?: -1L
    private val incomingChecklist = (savedStateHandle.get<Int>("checklist") ?: 0) == 1
    private val draft = MutableStateFlow(
        EditorUiState(
            isNew = incomingId <= 0L,
            isChecklist = incomingChecklist,
            body = if (incomingChecklist) ChecklistFormat.encode(listOf(ChecklistItem(""))) else ""
        )
    )

    val uiState: StateFlow<EditorUiState> = combine(
        draft,
        folderRepository.observeAll()
    ) { current, folders ->
        current.copy(folders = folders)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), draft.value)

    init {
        if (incomingId > 0L) {
            viewModelScope.launch {
                val note = noteRepository.getById(incomingId)
                if (note != null) {
                    draft.value = note.toEditorState(isNew = false)
                }
            }
        }
    }

    fun onTitleChange(value: String) = draft.update { it.copy(title = value) }
    fun onBodyChange(value: String) = draft.update { it.copy(body = value) }
    fun checklistItems(): List<ChecklistItem> = ChecklistFormat.parse(draft.value.body)
    fun onChecklistItemsChange(items: List<ChecklistItem>) {
        draft.update { it.copy(body = ChecklistFormat.encode(items)) }
    }
    fun onFolderChange(folderId: Long?) = draft.update { it.copy(folderId = folderId) }
    fun onColorChange(hex: String) = draft.update { it.copy(colorHex = hex) }
    fun onPinnedChange(pinned: Boolean) = draft.update { it.copy(isPinned = pinned) }
    fun onReminderChange(millis: Long?) = draft.update { it.copy(reminderAt = millis) }
    fun onMeetingPlaceChange(value: String) = draft.update {
        it.copy(meetingPlace = value, meetingLat = null, meetingLng = null)
    }
    fun onLocationReminderChange(enabled: Boolean) = draft.update { it.copy(locationReminder = enabled) }
    fun onRecurrenceChange(recurrence: Recurrence) = draft.update { it.copy(recurrence = recurrence) }
    fun clearMessage() = draft.update { it.copy(message = null) }

    fun save() {
        viewModelScope.launch { persist() }
    }

    suspend fun persist(): Long {
        val state = draft.value
        val now = System.currentTimeMillis()
        val note = state.toNote(now)
        val id = saveNote(note)
        if (id == 0L) return 0L
        val saved = noteRepository.getById(id) ?: note.copy(id = id)
        draft.update {
            saved.toEditorState(isNew = false).copy(folders = it.folders)
        }
        return id
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            val state = draft.value
            if (state.noteId > 0L) {
                deleteNote(state.toNote(System.currentTimeMillis()))
            }
            onDone()
        }
    }

    fun archive(onDone: () -> Unit) {
        viewModelScope.launch {
            val id = persist()
            if (id > 0L) {
                noteRepository.setArchived(id, !draft.value.isArchived)
            }
            onDone()
        }
    }

    fun copyToFolder(folderId: Long?) {
        viewModelScope.launch {
            val id = persist().takeIf { it > 0L } ?: persistEmpty()
            if (id > 0L) {
                noteRepository.copyToFolder(id, folderId)
            }
        }
    }

    fun addAttachment(uri: Uri, mimeType: String?) {
        viewModelScope.launch {
            draft.update { it.copy(busy = true) }
            val id = persist().takeIf { it > 0L } ?: persistEmpty()
            if (id <= 0L) {
                draft.update { it.copy(busy = false) }
                return@launch
            }
            runCatching { noteRepository.addAttachment(id, uri, mimeType) }
            val saved = noteRepository.getById(id)
            if (saved != null) {
                draft.update { saved.toEditorState(isNew = false).copy(folders = it.folders, busy = false) }
            } else {
                draft.update { it.copy(busy = false) }
            }
        }
    }

    fun removeAttachment(attachment: NoteAttachment) {
        viewModelScope.launch {
            noteRepository.deleteAttachment(attachment)
            draft.update { state ->
                state.copy(attachments = state.attachments.filterNot { it.id == attachment.id })
            }
        }
    }

    fun resolveMeetingPlace() {
        viewModelScope.launch {
            val query = draft.value.meetingPlace
            val point = meetingPlaceLocator.geocode(query)
            if (point == null) {
                draft.update { it.copy(message = context.getString(com.breez.notes.R.string.meeting_not_found)) }
                return@launch
            }
            draft.update {
                it.copy(
                    meetingLat = point.latitude,
                    meetingLng = point.longitude,
                    meetingPlace = point.label.ifBlank { it.meetingPlace },
                    message = context.getString(com.breez.notes.R.string.meeting_found)
                )
            }
        }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            val point = meetingPlaceLocator.currentLocation()
            if (point == null) {
                draft.update { it.copy(message = context.getString(com.breez.notes.R.string.meeting_location_unavailable)) }
                return@launch
            }
            val label = meetingPlaceLocator.reverseLabel(point.latitude, point.longitude)
                .ifBlank { context.getString(com.breez.notes.R.string.meeting_current_label) }
            draft.update {
                it.copy(
                    meetingLat = point.latitude,
                    meetingLng = point.longitude,
                    meetingPlace = label
                )
            }
        }
    }

    private suspend fun persistEmpty(): Long {
        val state = draft.value
        val now = System.currentTimeMillis()
        val id = saveNote(state.toNote(now), allowEmpty = true)
        draft.update { it.copy(noteId = id, isNew = false, createdAt = if (state.isNew) now else state.createdAt) }
        return id
    }

    private fun EditorUiState.toNote(now: Long): Note = Note(
        id = noteId,
        title = title,
        body = body,
        folderId = folderId,
        colorHex = colorHex,
        isPinned = isPinned,
        sortOrder = sortOrder,
        reminderAt = reminderAt,
        meetingPlace = meetingPlace,
        meetingLat = meetingLat,
        meetingLng = meetingLng,
        locationReminder = locationReminder,
        recurrence = if (reminderAt == null) Recurrence() else recurrence,
        attachments = attachments,
        isChecklist = isChecklist,
        isArchived = isArchived,
        createdAt = if (isNew) now else createdAt,
        updatedAt = now
    )

    private fun Note.toEditorState(isNew: Boolean): EditorUiState = EditorUiState(
        noteId = id,
        title = title,
        body = body,
        folderId = folderId,
        colorHex = colorHex,
        isPinned = isPinned,
        sortOrder = sortOrder,
        reminderAt = reminderAt,
        meetingPlace = meetingPlace,
        meetingLat = meetingLat,
        meetingLng = meetingLng,
        locationReminder = locationReminder,
        recurrence = recurrence,
        attachments = attachments,
        isChecklist = isChecklist,
        isArchived = isArchived,
        createdAt = createdAt,
        isNew = isNew
    )
}
