package com.breez.notes.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.domain.model.ThemeSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.themeDataStore

    val settings: Flow<ThemeSettings> = dataStore.data.map { prefs ->
        val modeName = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeSettings(
            themeMode = runCatching { ThemeMode.valueOf(modeName) }.getOrDefault(ThemeMode.SYSTEM),
            paletteId = prefs[KEY_PALETTE_ID] ?: "breez_blue",
            customColorHex = prefs[KEY_CUSTOM_COLOR],
            useMaterialYou = prefs[KEY_MATERIAL_YOU] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setPaletteId(id: String) {
        dataStore.edit { it[KEY_PALETTE_ID] = id }
    }

    suspend fun setCustomColorHex(hex: String) {
        dataStore.edit {
            it[KEY_CUSTOM_COLOR] = hex
            it[KEY_PALETTE_ID] = "custom"
        }
    }

    suspend fun setUseMaterialYou(enabled: Boolean) {
        dataStore.edit { it[KEY_MATERIAL_YOU] = enabled }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("themeMode")
        val KEY_PALETTE_ID = stringPreferencesKey("paletteId")
        val KEY_CUSTOM_COLOR = stringPreferencesKey("customColorHex")
        val KEY_MATERIAL_YOU = booleanPreferencesKey("useMaterialYou")
    }
}
