package com.breez.notes.domain.model

enum class FolderMarkType {
    COLOR,
    PHOTO,
    VIDEO
}

data class Folder(
    val id: Long = 0L,
    val name: String,
    val colorHex: String = DEFAULT_FOLDER_COLOR,
    val markType: FolderMarkType = FolderMarkType.COLOR,
    val markFileName: String = "",
    val noteCount: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val hasMediaMark: Boolean
        get() = markType != FolderMarkType.COLOR && markFileName.isNotBlank()

    companion object {
        const val DEFAULT_FOLDER_COLOR = "#7ED9C4"
        const val MAX_VIDEO_DURATION_MS = 10_000L
    }
}
