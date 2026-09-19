package com.breez.notes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val body: String,
    val folderId: Long?,
    val colorHex: String,
    val isPinned: Boolean,
    val sortOrder: Int = 0,
    val reminderAt: Long?,
    val meetingPlace: String?,
    val meetingLat: Double?,
    val meetingLng: Double?,
    val locationReminder: Boolean,
    val repeatUnit: String,
    val repeatInterval: Int,
    val repeatWeekDays: String,
    val repeatUntilAt: Long?,
    val isChecklist: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
