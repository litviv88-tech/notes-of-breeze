package com.breez.notes.domain.model

object Palettes {
    val all: List<Palette> = listOf(
        Palette("breez_blue", "palette_breez_blue", "#4A90E2"),
        Palette("mint", "palette_mint", "#7ED9C4"),
        Palette("lavender", "palette_lavender", "#A78BFA"),
        Palette("coral", "palette_coral", "#FF7A85"),
        Palette("sunset", "palette_sunset", "#FFB347"),
        Palette("forest", "palette_forest", "#4ADE80"),
        Palette("ocean", "palette_ocean", "#06B6D4"),
        Palette("rose", "palette_rose", "#F472B6"),
        Palette("amber", "palette_amber", "#FBBF24"),
        Palette("slate", "palette_slate", "#64748B"),
        Palette("cherry", "palette_cherry", "#EF4444"),
        Palette("monochrome", "palette_monochrome", "#94A3B8")
    )

    fun find(id: String): Palette = all.firstOrNull { it.id == id } ?: all.first()
}
