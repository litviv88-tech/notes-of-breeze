package com.breez.notes.domain.model

object Palettes {
    val all: List<Palette> = listOf(
        Palette("breez_blue", "palette_breez_blue", "#4A90E2")
    )

    fun find(id: String): Palette = all.firstOrNull { it.id == id } ?: all.first()
}
