package com.breez.notes.reminders

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.breez.notes.domain.model.Note
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client by lazy { LocationServices.getGeofencingClient(context) }

    @SuppressLint("MissingPermission")
    fun register(note: Note) {
        if (!note.locationReminder || !note.hasCoordinates) {
            remove(note.id)
            return
        }
        val lat = note.meetingLat ?: return
        val lng = note.meetingLng ?: return
        val geofence = Geofence.Builder()
            .setRequestId(requestId(note.id))
            .setCircularRegion(lat, lng, RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        runCatching {
            client.addGeofences(request, pendingIntent())
        }
    }

    fun remove(noteId: Long) {
        runCatching {
            client.removeGeofences(listOf(requestId(noteId)))
        }
    }

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION
        }
        return PendingIntent.getBroadcast(
            context,
            44,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    companion object {
        const val ACTION = "com.breez.notes.ACTION_GEOFENCE"
        const val RADIUS_METERS = 150f
        fun requestId(noteId: Long) = "note_$noteId"
        fun noteIdFrom(requestId: String): Long =
            requestId.removePrefix("note_").toLongOrNull() ?: 0L
    }
}
