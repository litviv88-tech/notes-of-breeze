package com.breez.notes.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.breez.notes.data.local.BreezDatabase
import com.breez.notes.data.local.dao.AttachmentDao
import com.breez.notes.data.local.dao.FolderDao
import com.breez.notes.data.local.dao.NoteDao
import com.breez.notes.R
import com.breez.notes.data.local.dao.WidgetConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BreezDatabase {
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT INTO folders (name, colorHex, markType, markFileName, createdAt, sortOrder) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf(
                        context.getString(R.string.folder_default_name),
                        "#7ED9C4",
                        "COLOR",
                        "",
                        now,
                        0
                    )
                )
            }
        }
        // fallbackToDestructiveMigration запрещён: неизвестная схема не должна стирать заметки.
        return Room.databaseBuilder(context, BreezDatabase::class.java, "breez_notes.db")
            .addCallback(callback)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .setQueryExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @Provides
    fun provideNoteDao(database: BreezDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideFolderDao(database: BreezDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideWidgetConfigDao(database: BreezDatabase): WidgetConfigDao = database.widgetConfigDao()

    @Provides
    fun provideAttachmentDao(database: BreezDatabase): AttachmentDao = database.attachmentDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE widget_configs ADD COLUMN cellWidth INTEGER NOT NULL DEFAULT 3")
            db.execSQL("ALTER TABLE widget_configs ADD COLUMN cellHeight INTEGER NOT NULL DEFAULT 2")
            db.execSQL("ALTER TABLE widget_configs ADD COLUMN backgroundScale REAL NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE widget_configs ADD COLUMN backgroundOffsetX REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE widget_configs ADD COLUMN backgroundOffsetY REAL NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE notes ADD COLUMN meetingPlace TEXT")
            db.execSQL("ALTER TABLE notes ADD COLUMN meetingLat REAL")
            db.execSQL("ALTER TABLE notes ADD COLUMN meetingLng REAL")
            db.execSQL("ALTER TABLE notes ADD COLUMN locationReminder INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE notes ADD COLUMN repeatUnit TEXT NOT NULL DEFAULT 'NONE'")
            db.execSQL("ALTER TABLE notes ADD COLUMN repeatInterval INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE notes ADD COLUMN repeatWeekDays TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE notes ADD COLUMN repeatUntilAt INTEGER")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS attachments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    noteId INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    fileName TEXT NOT NULL,
                    mimeType TEXT NOT NULL,
                    ocrText TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(noteId) REFERENCES notes(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_noteId ON attachments(noteId)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE folders ADD COLUMN markType TEXT NOT NULL DEFAULT 'COLOR'")
            db.execSQL("ALTER TABLE folders ADD COLUMN markFileName TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE notes ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE folders ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            db.query("SELECT id FROM notes ORDER BY isPinned DESC, updatedAt DESC").use { cursor ->
                var order = 0
                while (cursor.moveToNext()) {
                    db.execSQL(
                        "UPDATE notes SET sortOrder = ? WHERE id = ?",
                        arrayOf(order, cursor.getLong(0))
                    )
                    order++
                }
            }
            db.query("SELECT id FROM folders ORDER BY createdAt ASC").use { cursor ->
                var order = 0
                while (cursor.moveToNext()) {
                    db.execSQL(
                        "UPDATE folders SET sortOrder = ? WHERE id = ?",
                        arrayOf(order, cursor.getLong(0))
                    )
                    order++
                }
            }
        }
    }
}
