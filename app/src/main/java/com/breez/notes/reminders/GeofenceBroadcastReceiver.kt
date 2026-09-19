package com.breez.notes.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.breez.notes.BreezApplication
import com.breez.notes.MainActivity
import com.breez.notes.R
import com.breez.notes.di.ReminderEntryPoint
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL
        ) return
        val ids = event.triggeringGeofences.orEmpty().map { GeofenceManager.noteIdFrom(it.requestId) }
        if (ids.isEmpty()) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entry = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ReminderEntryPoint::class.java
                )
                for (noteId in ids) {
                    val note = entry.noteRepository().getById(noteId) ?: continue
                    if (!note.locationReminder) continue
                    showNotification(context, note.id, note.title, note.meetingPlace)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, noteId: Long, title: String, place: String) {
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            (noteId + 10_000).toInt(),
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val placeText = place.ifBlank { context.getString(R.string.meeting_nearby_fallback) }
        val notification = NotificationCompat.Builder(context, BreezApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(
                title.ifBlank { context.getString(R.string.note_untitled) }
            )
            .setContentText(context.getString(R.string.meeting_nearby_text, placeText))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify((noteId + 20_000).toInt(), notification)
    }
}
