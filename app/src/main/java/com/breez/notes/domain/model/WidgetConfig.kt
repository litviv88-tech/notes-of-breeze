package com.breez.notes.domain.model

enum class WidgetSourceType {
    ALL,
    FOLDER,
    NOTE
}

enum class WidgetDisplayMode {
    TITLE,
    FULL,
    CHECKLIST
}

data class WidgetConfig(
    val id: Long = 0L,
    val appWidgetId: Int,
    val sourceType: WidgetSourceType = WidgetSourceType.ALL,
    val folderId: Long? = null,
    val noteId: Long? = null,
    val backgroundUri: String? = null,
    val backgroundOpacity: Float = 0.45f,
    val textColorHex: String = DEFAULT_TEXT_COLOR,
    val fontSizeSp: Int = 14,
    val displayMode: WidgetDisplayMode = WidgetDisplayMode.TITLE,
    val maxNotes: Int = 3,
    val cornerRadiusDp: Int = 16,
    val cellWidth: Int = DEFAULT_CELL_WIDTH,
    val cellHeight: Int = DEFAULT_CELL_HEIGHT,
    val backgroundScale: Float = 1f,
    val backgroundOffsetX: Float = 0f,
    val backgroundOffsetY: Float = 0f
) {
    companion object {
        const val DEFAULT_TEXT_COLOR = "#FFFFFF"
        const val DEFAULT_CELL_WIDTH = 3
        const val DEFAULT_CELL_HEIGHT = 2

        fun default(appWidgetId: Int): WidgetConfig = WidgetConfig(appWidgetId = appWidgetId)
    }
}
