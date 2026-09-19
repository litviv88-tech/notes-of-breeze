package com.breez.notes.ui.theme

import com.breez.notes.R
import com.breez.notes.domain.model.Palette

fun Palette.titleRes(): Int = when (id) {
    "mint" -> R.string.palette_mint
    "lavender" -> R.string.palette_lavender
    "coral" -> R.string.palette_coral
    "sunset" -> R.string.palette_sunset
    "forest" -> R.string.palette_forest
    "ocean" -> R.string.palette_ocean
    "rose" -> R.string.palette_rose
    "amber" -> R.string.palette_amber
    "slate" -> R.string.palette_slate
    "cherry" -> R.string.palette_cherry
    "monochrome" -> R.string.palette_monochrome
    else -> R.string.palette_breez_blue
}
