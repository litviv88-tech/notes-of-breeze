package com.breez.notes.domain.usecase

import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.ReminderScheduler
import javax.inject.Inject

class SetReminder @Inject constructor(
    private val reminders: ReminderScheduler
) {
    suspend operator fun invoke(note: Note, replaceExisting: Boolean = true) {
        reminders.schedule(note, replaceExisting)
    }
}
