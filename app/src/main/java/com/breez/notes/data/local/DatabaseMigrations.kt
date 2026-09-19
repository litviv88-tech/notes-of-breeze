package com.breez.notes.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfMissing("widget_configs", "cellWidth", "INTEGER NOT NULL DEFAULT 3")
            db.addColumnIfMissing("widget_configs", "cellHeight", "INTEGER NOT NULL DEFAULT 2")
            db.addColumnIfMissing("widget_configs", "backgroundScale", "REAL NOT NULL DEFAULT 1")
            db.addColumnIfMissing("widget_configs", "backgroundOffsetX", "REAL NOT NULL DEFAULT 0")
            db.addColumnIfMissing("widget_configs", "backgroundOffsetY", "REAL NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfMissing("notes", "meetingPlace", "TEXT")
            db.addColumnIfMissing("notes", "meetingLat", "REAL")
            db.addColumnIfMissing("notes", "meetingLng", "REAL")
            db.addColumnIfMissing("notes", "locationReminder", "INTEGER NOT NULL DEFAULT 0")
            db.addColumnIfMissing("notes", "repeatUnit", "TEXT NOT NULL DEFAULT 'NONE'")
            db.addColumnIfMissing("notes", "repeatInterval", "INTEGER NOT NULL DEFAULT 1")
            db.addColumnIfMissing("notes", "repeatWeekDays", "TEXT NOT NULL DEFAULT ''")
            db.addColumnIfMissing("notes", "repeatUntilAt", "INTEGER")
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
            db.ensureIndex("index_attachments_noteId", "attachments", "noteId")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfMissing("folders", "markType", "TEXT NOT NULL DEFAULT 'COLOR'")
            db.addColumnIfMissing("folders", "markFileName", "TEXT NOT NULL DEFAULT ''")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val notesOrderAdded = db.addColumnIfMissing(
                "notes",
                "sortOrder",
                "INTEGER NOT NULL DEFAULT 0"
            )
            val foldersOrderAdded = db.addColumnIfMissing(
                "folders",
                "sortOrder",
                "INTEGER NOT NULL DEFAULT 0"
            )
            if (notesOrderAdded) {
                db.backfillOrder("notes", "isPinned DESC, updatedAt DESC")
            }
            if (foldersOrderAdded) {
                db.backfillOrder("folders", "createdAt ASC")
            }
            db.ensureCurrentIndexes()
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfMissing("notes", "isChecklist", "INTEGER NOT NULL DEFAULT 0")
            db.ensureCurrentIndexes()
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.addColumnIfMissing("notes", "isArchived", "INTEGER NOT NULL DEFAULT 0")
            db.ensureCurrentIndexes()
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7
    )

    private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean {
        query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex < 0) return false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    private fun SupportSQLiteDatabase.addColumnIfMissing(
        table: String,
        column: String,
        spec: String
    ): Boolean {
        if (hasColumn(table, column)) return false
        execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $spec")
        return true
    }

    private fun SupportSQLiteDatabase.ensureIndex(
        name: String,
        table: String,
        columns: String,
        unique: Boolean = false
    ) {
        val uniqueSql = if (unique) "UNIQUE " else ""
        execSQL("CREATE ${uniqueSql}INDEX IF NOT EXISTS `$name` ON `$table` ($columns)")
    }

    private fun SupportSQLiteDatabase.ensureCurrentIndexes() {
        ensureIndex("index_notes_folderId", "notes", "folderId")
        ensureIndex("index_widget_configs_folderId", "widget_configs", "folderId")
        ensureIndex("index_widget_configs_noteId", "widget_configs", "noteId")
        ensureIndex("index_widget_configs_appWidgetId", "widget_configs", "appWidgetId", unique = true)
        ensureIndex("index_attachments_noteId", "attachments", "noteId")
    }

    private fun SupportSQLiteDatabase.backfillOrder(table: String, orderBy: String) {
        query("SELECT id FROM `$table` ORDER BY $orderBy").use { cursor ->
            var order = 0
            while (cursor.moveToNext()) {
                execSQL(
                    "UPDATE `$table` SET sortOrder = ? WHERE id = ?",
                    arrayOf(order, cursor.getLong(0))
                )
                order++
            }
        }
    }
}
