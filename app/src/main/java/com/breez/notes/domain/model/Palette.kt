package com.breez.notes.domain.model

data class Palette(
    val id: String,
    val nameRes: Int,
    val primaryHex: String
) {
    companion object {
        const val CUSTOM_ID = "custom"
    }
}
