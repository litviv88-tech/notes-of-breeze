package com.breez.notes.domain.usecase

import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.ReminderScheduler
import javax.inject.Inject

class DeleteNote @Inject constructor(
    private val notes: NoteRepository,
    private val reminders: ReminderScheduler
) {
    suspend operator fun invoke(note: Note) {
        reminders.cancel(note.id)
        notes.delete(note)
    }
}
