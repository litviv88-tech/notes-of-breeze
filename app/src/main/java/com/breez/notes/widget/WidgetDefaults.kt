package com.breez.notes.widget

import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.model.WidgetDisplayMode

fun defaultWidgetConfig(appWidgetId: Int, kind: WidgetKind): WidgetConfig {
    return when (kind) {
        WidgetKind.NOTES -> WidgetConfig.default(appWidgetId)
        WidgetKind.TODO -> WidgetConfig(
            appWidgetId = appWidgetId,
            displayMode = WidgetDisplayMode.CHECKLIST,
            maxNotes = 5,
            cellWidth = WidgetCells.DEFAULT_WIDTH,
            cellHeight = 3
        )
    }
}
