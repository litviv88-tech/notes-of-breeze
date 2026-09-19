package com.breez.notes.platform.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.breez.notes.BreezApplication
import com.breez.notes.MainActivity
import com.breez.notes.R
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.usecase.SetReminder
import com.breez.notes.reminders.RecurrenceCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val setReminder: SetReminder
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong(KEY_NOTE_ID, 0L)
        val note = if (noteId > 0L) noteRepository.getById(noteId) else null
        val title = note?.title?.ifBlank { null }
            ?: inputData.getString(KEY_TITLE).orEmpty().ifBlank {
                context.getString(R.string.note_untitled)
            }
        val body = note?.body ?: inputData.getString(KEY_BODY).orEmpty()
        val extra = note?.meetingPlace?.takeIf { it.isNotBlank() }
            ?.let { context.getString(R.string.meeting_place_prefix, it) }
            .orEmpty()
        val text = listOf(body, extra).filter { it.isNotBlank() }.joinToString("\n")
            .ifBlank { context.getString(R.string.reminder_notification_title) }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, BreezApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return Result.retry()
        manager.notify(noteId.toInt().let { if (it == 0) 42 else it }, notification)
        if (note != null) {
            val now = System.currentTimeMillis()
            val next = RecurrenceCalculator.nextUpcoming(
                fromMillis = note.reminderAt ?: now,
                recurrence = note.recurrence,
                now = now
            )
            val updated = note.copy(reminderAt = next, updatedAt = now)
            noteRepository.upsert(updated)
            setReminder(updated, replaceExisting = false)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_NOTE_ID = "noteId"
    }
}
