package com.breez.notes.di

import com.breez.notes.data.repository.FolderRepositoryImpl
import com.breez.notes.data.repository.NoteRepositoryImpl
import com.breez.notes.data.repository.ThemeRepositoryImpl
import com.breez.notes.data.repository.UpdateRepositoryImpl
import com.breez.notes.data.repository.WidgetRepositoryImpl
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.ReminderScheduler
import com.breez.notes.domain.repository.ThemeRepository
import com.breez.notes.domain.repository.UpdateRepository
import com.breez.notes.domain.repository.WidgetRepository
import com.breez.notes.reminders.ReminderCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindWidgetRepository(impl: WidgetRepositoryImpl): WidgetRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: ReminderCoordinator): ReminderScheduler
}
