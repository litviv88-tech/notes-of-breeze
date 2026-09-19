package com.breez.notes.domain.model

data class Note(
    val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    val folderId: Long? = null,
    val colorHex: String = DEFAULT_NOTE_COLOR,
    val isPinned: Boolean = false,
    val sortOrder: Int = 0,
    val reminderAt: Long? = null,
    val meetingPlace: String = "",
    val meetingLat: Double? = null,
    val meetingLng: Double? = null,
    val locationReminder: Boolean = false,
    val recurrence: Recurrence = Recurrence(),
    val attachments: List<NoteAttachment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val hasMeetingPlace: Boolean get() = meetingPlace.isNotBlank() || (meetingLat != null && meetingLng != null)
    val hasCoordinates: Boolean get() = meetingLat != null && meetingLng != null
    val hasContent: Boolean
        get() = title.isNotBlank() ||
            body.isNotBlank() ||
            meetingPlace.isNotBlank() ||
            attachments.isNotEmpty() ||
            reminderAt != null

    companion object {
        const val DEFAULT_NOTE_COLOR = "#4A90E2"
    }
}
