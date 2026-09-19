package com.breez.notes.domain.repository

import com.breez.notes.domain.model.Note

interface ReminderScheduler {
    fun schedule(note: Note, replaceExisting: Boolean = true)
    fun cancel(noteId: Long)
}
