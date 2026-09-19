package com.breez.notes.domain.usecase

import com.breez.notes.domain.fakes.FakeNoteRepository
import com.breez.notes.domain.fakes.FakeReminderScheduler
import com.breez.notes.domain.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteNoteTest {

    @Test
    fun cancelsReminderThenDeletesNote() = runTest {
        val notes = FakeNoteRepository()
        val reminders = FakeReminderScheduler()
        val id = notes.upsert(Note(title = "Удалить"))
        val note = notes.getById(id) ?: error("note missing")

        DeleteNote(notes, reminders)(note)

        assertTrue(notes.getAll().isEmpty())
        assertEquals(listOf(id), reminders.cancelled)
    }
}
