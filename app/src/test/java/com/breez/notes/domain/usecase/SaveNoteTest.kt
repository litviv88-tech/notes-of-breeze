package com.breez.notes.domain.usecase

import com.breez.notes.domain.fakes.FakeNoteRepository
import com.breez.notes.domain.fakes.FakeReminderScheduler
import com.breez.notes.domain.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveNoteTest {

    @Test
    fun skipsEmptyNewNote() = runTest {
        val notes = FakeNoteRepository()
        val reminders = FakeReminderScheduler()
        val saveNote = SaveNote(notes, reminders)

        val id = saveNote(Note())

        assertEquals(0L, id)
        assertTrue(notes.getAll().isEmpty())
        assertTrue(reminders.scheduled.isEmpty())
    }

    @Test
    fun savesContentAndSchedulesReminder() = runTest {
        val notes = FakeNoteRepository()
        val reminders = FakeReminderScheduler()
        val saveNote = SaveNote(notes, reminders)
        val reminderAt = 1_700_000_000_000L

        val id = saveNote(Note(title = "Купить хлеб", reminderAt = reminderAt))

        assertTrue(id > 0L)
        val saved = notes.getById(id)
        assertEquals("Купить хлеб", saved?.title)
        assertEquals(1, reminders.scheduled.size)
        assertEquals(id, reminders.scheduled.first().id)
        assertEquals(reminderAt, reminders.scheduled.first().reminderAt)
    }

    @Test
    fun allowEmptyStillPersistsDraft() = runTest {
        val notes = FakeNoteRepository()
        val reminders = FakeReminderScheduler()
        val saveNote = SaveNote(notes, reminders)

        val id = saveNote(Note(), allowEmpty = true)

        assertTrue(id > 0L)
        assertEquals(1, notes.getAll().size)
        assertEquals(1, reminders.scheduled.size)
    }
}
