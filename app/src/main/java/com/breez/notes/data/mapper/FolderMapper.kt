package com.breez.notes.data.mapper

import com.breez.notes.data.local.dao.FolderWithCount
import com.breez.notes.data.local.entity.FolderEntity
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType

fun FolderWithCount.toDomain(): Folder = Folder(
    id = id,
    name = name,
    colorHex = colorHex,
    markType = markType.toFolderMarkType(),
    markFileName = markFileName,
    noteCount = noteCount,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun FolderEntity.toDomain(noteCount: Int = 0): Folder = Folder(
    id = id,
    name = name,
    colorHex = colorHex,
    markType = markType.toFolderMarkType(),
    markFileName = markFileName,
    noteCount = noteCount,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    markType = markType.name,
    markFileName = markFileName,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun String.toFolderMarkType(): FolderMarkType =
    runCatching { FolderMarkType.valueOf(this) }.getOrDefault(FolderMarkType.COLOR)
