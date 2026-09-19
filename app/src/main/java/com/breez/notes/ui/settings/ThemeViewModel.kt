package com.breez.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.data.repository.PaletteRepository
import com.breez.notes.domain.model.Palette
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.domain.model.ThemeSettings
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val paletteRepository: PaletteRepository
) : ViewModel() {

    val settings: StateFlow<ThemeSettings> = themeRepository.themeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeSettings())

    val wallpaper: StateFlow<WallpaperSettings> = themeRepository.wallpaperSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperSettings())

    val palettes: List<Palette> = paletteRepository.palettes

    fun paletteFor(id: String): Palette = paletteRepository.find(id)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeRepository.setThemeMode(mode) }
    }

    fun setPalette(id: String) {
        viewModelScope.launch { themeRepository.setPaletteId(id) }
    }

    fun setCustomColor(hex: String) {
        viewModelScope.launch { themeRepository.setCustomColorHex(hex) }
    }

    fun setMaterialYou(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setUseMaterialYou(enabled) }
    }
}
