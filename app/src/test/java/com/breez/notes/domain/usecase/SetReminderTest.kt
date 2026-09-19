package com.breez.notes.domain.usecase

import com.breez.notes.domain.fakes.FakeReminderScheduler
import com.breez.notes.domain.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetReminderTest {

    @Test
    fun schedulesThroughBoundary() = runTest {
        val reminders = FakeReminderScheduler()
        val note = Note(id = 7L, title = "Встреча", reminderAt = 42L)

        SetReminder(reminders)(note)

        assertEquals(1, reminders.scheduled.size)
        assertEquals(7L, reminders.scheduled.first().id)
        assertEquals(42L, reminders.scheduled.first().reminderAt)
    }
}
