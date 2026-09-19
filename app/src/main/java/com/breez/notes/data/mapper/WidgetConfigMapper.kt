package com.breez.notes.data.mapper

import com.breez.notes.data.local.entity.WidgetConfigEntity
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.model.WidgetDisplayMode
import com.breez.notes.domain.model.WidgetSourceType

fun WidgetConfigEntity.toDomain(): WidgetConfig = WidgetConfig(
    id = id,
    appWidgetId = appWidgetId,
    sourceType = runCatching { WidgetSourceType.valueOf(sourceType) }.getOrDefault(WidgetSourceType.ALL),
    folderId = folderId,
    noteId = noteId,
    backgroundUri = backgroundUri,
    backgroundOpacity = backgroundOpacity,
    textColorHex = textColorHex,
    fontSizeSp = fontSizeSp,
    displayMode = runCatching { WidgetDisplayMode.valueOf(displayMode) }.getOrDefault(WidgetDisplayMode.TITLE),
    maxNotes = maxNotes,
    cornerRadiusDp = cornerRadiusDp,
    cellWidth = cellWidth,
    cellHeight = cellHeight,
    backgroundScale = backgroundScale,
    backgroundOffsetX = backgroundOffsetX,
    backgroundOffsetY = backgroundOffsetY
)

fun WidgetConfig.toEntity(): WidgetConfigEntity = WidgetConfigEntity(
    id = id,
    appWidgetId = appWidgetId,
    sourceType = sourceType.name,
    folderId = folderId,
    noteId = noteId,
    backgroundUri = backgroundUri,
    backgroundOpacity = backgroundOpacity,
    textColorHex = textColorHex,
    fontSizeSp = fontSizeSp,
    displayMode = displayMode.name,
    maxNotes = maxNotes,
    cornerRadiusDp = cornerRadiusDp,
    cellWidth = cellWidth,
    cellHeight = cellHeight,
    backgroundScale = backgroundScale,
    backgroundOffsetX = backgroundOffsetX,
    backgroundOffsetY = backgroundOffsetY
)
