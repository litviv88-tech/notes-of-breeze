package com.breez.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val draft = MutableStateFlow(WallpaperSettings())

    val preview: StateFlow<WallpaperSettings> = draft
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperSettings())

    init {
        viewModelScope.launch {
            themeRepository.wallpaperSettings.collect { settings ->
                draft.value = settings
            }
        }
    }

    fun update(block: (WallpaperSettings) -> WallpaperSettings) {
        draft.update(block)
    }

    fun apply() {
        viewModelScope.launch { themeRepository.setWallpaper(draft.value) }
    }
}
