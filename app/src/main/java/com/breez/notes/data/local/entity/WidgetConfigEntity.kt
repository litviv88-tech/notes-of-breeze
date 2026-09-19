package com.breez.notes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "widget_configs",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId"), Index("noteId"), Index(value = ["appWidgetId"], unique = true)]
)
data class WidgetConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val appWidgetId: Int,
    val sourceType: String,
    val folderId: Long?,
    val noteId: Long?,
    val backgroundUri: String?,
    val backgroundOpacity: Float,
    val textColorHex: String,
    val fontSizeSp: Int,
    val displayMode: String,
    val maxNotes: Int,
    val cornerRadiusDp: Int,
    val cellWidth: Int = 3,
    val cellHeight: Int = 2,
    val backgroundScale: Float = 1f,
    val backgroundOffsetX: Float = 0f,
    val backgroundOffsetY: Float = 0f
)
