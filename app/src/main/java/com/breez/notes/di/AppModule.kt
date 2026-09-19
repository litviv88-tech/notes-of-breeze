package com.breez.notes.di

import android.content.Context
import androidx.work.WorkManager
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.WidgetRepository
import com.breez.notes.domain.usecase.DeleteNote
import com.breez.notes.reminders.ReminderCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetRepository(): WidgetRepository
    fun noteRepository(): NoteRepository
    fun folderRepository(): FolderRepository
    fun deleteNote(): DeleteNote
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun noteRepository(): NoteRepository
    fun reminderCoordinator(): ReminderCoordinator
}
