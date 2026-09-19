package com.breez.notes.domain.fakes

import com.breez.notes.domain.model.Note
import com.breez.notes.domain.repository.ReminderScheduler

class FakeReminderScheduler : ReminderScheduler {
    val scheduled = mutableListOf<Note>()
    val cancelled = mutableListOf<Long>()

    override fun schedule(note: Note, replaceExisting: Boolean) {
        scheduled += note
    }

    override fun cancel(noteId: Long) {
        cancelled += noteId
    }
}
