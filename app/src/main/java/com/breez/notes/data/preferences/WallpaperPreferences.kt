package com.breez.notes.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.model.WallpaperType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.wallpaperDataStore by preferencesDataStore(name = "wallpaper_prefs")

@Singleton
class WallpaperPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.wallpaperDataStore

    val settings: Flow<WallpaperSettings> = dataStore.data.map { prefs ->
        val typeName = prefs[KEY_TYPE] ?: WallpaperType.BUILTIN.name
        WallpaperSettings(
            wallpaperType = runCatching { WallpaperType.valueOf(typeName) }.getOrDefault(WallpaperType.BUILTIN),
            builtInWallpaperId = prefs[KEY_BUILTIN_ID] ?: "breeze_sky",
            wallpaperUri = prefs[KEY_URI],
            wallpaperUriDark = prefs[KEY_URI_DARK],
            wallpaperColorHex = prefs[KEY_COLOR] ?: "#4A90E2",
            wallpaperDim = prefs[KEY_DIM] ?: 0.25f,
            wallpaperBlur = prefs[KEY_BLUR] ?: 0f
        )
    }

    suspend fun setWallpaper(settings: WallpaperSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_TYPE] = settings.wallpaperType.name
            prefs[KEY_BUILTIN_ID] = settings.builtInWallpaperId
            prefs[KEY_DIM] = settings.wallpaperDim
            prefs[KEY_BLUR] = settings.wallpaperBlur
            val uri = settings.wallpaperUri
            if (uri == null) prefs.remove(KEY_URI) else prefs[KEY_URI] = uri
            val uriDark = settings.wallpaperUriDark
            if (uriDark == null) prefs.remove(KEY_URI_DARK) else prefs[KEY_URI_DARK] = uriDark
            val color = settings.wallpaperColorHex
            if (color == null) prefs.remove(KEY_COLOR) else prefs[KEY_COLOR] = color
        }
    }

    private companion object {
        val KEY_TYPE = stringPreferencesKey("wallpaperType")
        val KEY_BUILTIN_ID = stringPreferencesKey("builtInWallpaperId")
        val KEY_URI = stringPreferencesKey("wallpaperUri")
        val KEY_URI_DARK = stringPreferencesKey("wallpaperUriDark")
        val KEY_COLOR = stringPreferencesKey("wallpaperColorHex")
        val KEY_DIM = floatPreferencesKey("wallpaperDim")
        val KEY_BLUR = floatPreferencesKey("wallpaperBlur")
    }
}
