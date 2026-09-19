package com.breez.notes.domain.model

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val paletteId: String = "breez_blue",
    val customColorHex: String? = null,
    val useMaterialYou: Boolean = false
)
