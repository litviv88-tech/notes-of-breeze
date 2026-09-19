package com.breez.notes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorHex: String,
    val markType: String = "COLOR",
    val markFileName: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long
)
