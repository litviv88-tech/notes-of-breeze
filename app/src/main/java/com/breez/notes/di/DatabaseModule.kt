package com.breez.notes.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.breez.notes.R
import com.breez.notes.data.local.BreezDatabase
import com.breez.notes.data.local.DatabaseMigrations
import com.breez.notes.data.local.dao.AttachmentDao
import com.breez.notes.data.local.dao.FolderDao
import com.breez.notes.data.local.dao.NoteDao
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
            .addMigrations(*DatabaseMigrations.ALL)
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
}
