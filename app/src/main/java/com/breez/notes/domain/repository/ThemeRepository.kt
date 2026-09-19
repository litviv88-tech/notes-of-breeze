package com.breez.notes.domain.repository

import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.domain.model.ThemeSettings
import com.breez.notes.domain.model.WallpaperSettings
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeSettings: Flow<ThemeSettings>
    val wallpaperSettings: Flow<WallpaperSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setPaletteId(id: String)
    suspend fun setCustomColorHex(hex: String)
    suspend fun setUseMaterialYou(enabled: Boolean)
    suspend fun setWallpaper(settings: WallpaperSettings)
}
