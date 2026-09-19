package com.breez.notes.data.repository

import com.breez.notes.data.preferences.ThemePreferences
import com.breez.notes.data.preferences.WallpaperPreferences
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.domain.model.ThemeSettings
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val wallpaperPreferences: WallpaperPreferences
) : ThemeRepository {

    override val themeSettings: Flow<ThemeSettings> = themePreferences.settings
    override val wallpaperSettings: Flow<WallpaperSettings> = wallpaperPreferences.settings

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    override suspend fun setPaletteId(id: String) {
        themePreferences.setPaletteId(id)
    }

    override suspend fun setCustomColorHex(hex: String) {
        themePreferences.setCustomColorHex(hex)
    }

    override suspend fun setUseMaterialYou(enabled: Boolean) {
        themePreferences.setUseMaterialYou(enabled)
    }

    override suspend fun setWallpaper(settings: WallpaperSettings) {
        wallpaperPreferences.setWallpaper(settings)
    }
}
