package com.breez.notes.domain.usecase

import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.ReminderScheduler
import javax.inject.Inject

class SaveNote @Inject constructor(
    private val notes: NoteRepository,
    private val reminders: ReminderScheduler
) {
    suspend operator fun invoke(note: Note, allowEmpty: Boolean = false): Long {
        if (!allowEmpty && note.id == 0L && !note.hasContent) return 0L
        val id = notes.upsert(note)
        val saved = notes.getById(id) ?: note.copy(id = id)
        reminders.schedule(saved)
        return id
    }
}
