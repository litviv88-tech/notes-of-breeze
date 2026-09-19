package com.breez.notes.domain.model

enum class AttachmentType {
    PHOTO,
    VIDEO
}

data class NoteAttachment(
    val id: Long = 0L,
    val noteId: Long,
    val type: AttachmentType,
    val fileName: String,
    val mimeType: String,
    val ocrText: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isPhoto: Boolean get() = type == AttachmentType.PHOTO
    val isVideo: Boolean get() = type == AttachmentType.VIDEO
}
