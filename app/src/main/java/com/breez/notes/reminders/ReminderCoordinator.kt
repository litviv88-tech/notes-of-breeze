package com.breez.notes.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.breez.notes.R
import com.breez.notes.domain.model.Note
import com.breez.notes.ui.editor.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val geofenceManager: GeofenceManager
) {
    fun schedule(note: Note, replaceExisting: Boolean = true) {
        val tag = tag(note.id)
        if (replaceExisting) {
            workManager.cancelAllWorkByTag(tag)
        }
        val reminderAt = note.reminderAt
        if (reminderAt != null) {
            val delay = reminderAt - System.currentTimeMillis()
            if (delay > 0L) {
                val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            ReminderWorker.KEY_TITLE to note.title.ifBlank {
                                context.getString(R.string.note_untitled)
                            },
                            ReminderWorker.KEY_BODY to note.body,
                            ReminderWorker.KEY_NOTE_ID to note.id
                        )
                    )
                    .addTag(tag)
                    .build()
                workManager.enqueueUniqueWork(
                    workName(note.id, reminderAt),
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
        }
        geofenceManager.register(note)
    }

    fun cancel(noteId: Long) {
        workManager.cancelAllWorkByTag(tag(noteId))
        geofenceManager.remove(noteId)
    }

    private fun tag(noteId: Long) = "reminder_$noteId"
    private fun workName(noteId: Long, at: Long) = "reminder_${noteId}_$at"
}
