package com.breez.notes.data.repository

import com.breez.notes.R
import com.breez.notes.domain.model.Palette
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaletteRepository @Inject constructor() {

    val palettes: List<Palette> = listOf(
        Palette("breez_blue", R.string.palette_breez_blue, "#4A90E2"),
        Palette("mint", R.string.palette_mint, "#7ED9C4"),
        Palette("lavender", R.string.palette_lavender, "#A78BFA"),
        Palette("coral", R.string.palette_coral, "#FF7A85"),
        Palette("sunset", R.string.palette_sunset, "#FFB347"),
        Palette("forest", R.string.palette_forest, "#4ADE80"),
        Palette("ocean", R.string.palette_ocean, "#06B6D4"),
        Palette("rose", R.string.palette_rose, "#F472B6"),
        Palette("amber", R.string.palette_amber, "#FBBF24"),
        Palette("slate", R.string.palette_slate, "#64748B"),
        Palette("cherry", R.string.palette_cherry, "#EF4444"),
        Palette("monochrome", R.string.palette_monochrome, "#94A3B8")
    )

    fun find(id: String): Palette = palettes.firstOrNull { it.id == id } ?: palettes.first()
}
