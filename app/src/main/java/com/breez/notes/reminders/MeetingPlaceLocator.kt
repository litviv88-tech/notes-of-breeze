package com.breez.notes.reminders

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val label: String
)

@Singleton
class MeetingPlaceLocator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun geocode(query: String): GeoPoint? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || !Geocoder.isPresent()) return@withContext null
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(trimmed, 1) { list ->
                    continuation.resume(list.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(trimmed, 1)?.firstOrNull()
        } ?: return@withContext null
        GeoPoint(
            latitude = address.latitude,
            longitude = address.longitude,
            label = trimmed
        )
    }

    @SuppressLint("MissingPermission")
    fun currentLocation(): GeoPoint? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            ?: return null
        return GeoPoint(location.latitude, location.longitude, "")
    }

    suspend fun reverseLabel(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext ""
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(lat, lng, 1) { list ->
                    continuation.resume(list.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
        } ?: return@withContext ""
        listOfNotNull(
            address.thoroughfare,
            address.subThoroughfare,
            address.locality,
            address.adminArea
        ).joinToString(", ").ifBlank { address.getAddressLine(0).orEmpty() }
    }
}
