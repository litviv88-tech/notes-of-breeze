package com.breez.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.breez.notes.data.local.dao.AttachmentDao
import com.breez.notes.data.local.dao.FolderDao
import com.breez.notes.data.local.dao.NoteDao
import com.breez.notes.data.local.dao.WidgetConfigDao
import com.breez.notes.data.local.entity.AttachmentEntity
import com.breez.notes.data.local.entity.FolderEntity
import com.breez.notes.data.local.entity.NoteEntity
import com.breez.notes.data.local.entity.WidgetConfigEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        WidgetConfigEntity::class,
        AttachmentEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class BreezDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun attachmentDao(): AttachmentDao
}
