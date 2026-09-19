package com.breez.notes.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.breez.notes.R

data class BuiltInWallpaper(
    val id: String,
    val nameRes: Int,
    val colors: List<Color>
) {
    val brush: Brush
        get() = Brush.linearGradient(colors)
}

val BuiltInWallpapers = listOf(
    BuiltInWallpaper("breeze_sky", R.string.wallpaper_breeze_sky, listOf(Color(0xFF4A90E2), Color(0xFF7ED9C4))),
    BuiltInWallpaper("lavender_dream", R.string.wallpaper_lavender_dream, listOf(Color(0xFFA78BFA), Color(0xFFF472B6))),
    BuiltInWallpaper("coral_sunset", R.string.wallpaper_coral_sunset, listOf(Color(0xFFFF7A85), Color(0xFFFFB347))),
    BuiltInWallpaper("forest_mist", R.string.wallpaper_forest_mist, listOf(Color(0xFF14532D), Color(0xFF4ADE80))),
    BuiltInWallpaper("ocean_deep", R.string.wallpaper_ocean_deep, listOf(Color(0xFF0E7490), Color(0xFF06B6D4))),
    BuiltInWallpaper("rose_night", R.string.wallpaper_rose_night, listOf(Color(0xFF4A044E), Color(0xFFF472B6))),
    BuiltInWallpaper("amber_glow", R.string.wallpaper_amber_glow, listOf(Color(0xFFB45309), Color(0xFFFBBF24))),
    BuiltInWallpaper("slate_storm", R.string.wallpaper_slate_storm, listOf(Color(0xFF1E293B), Color(0xFF64748B))),
    BuiltInWallpaper("cherry_dusk", R.string.wallpaper_cherry_dusk, listOf(Color(0xFF7F1D1D), Color(0xFFEF4444))),
    BuiltInWallpaper("midnight_mono", R.string.wallpaper_midnight_mono, listOf(Color(0xFF0F172A), Color(0xFF94A3B8))),
    BuiltInWallpaper("aurora", R.string.wallpaper_aurora, listOf(Color(0xFF4A90E2), Color(0xFF4ADE80), Color(0xFFA78BFA))),
    BuiltInWallpaper("peach_cream", R.string.wallpaper_peach_cream, listOf(Color(0xFFFFE4D6), Color(0xFFFF7A85)))
)

fun wallpaperById(id: String): BuiltInWallpaper =
    BuiltInWallpapers.firstOrNull { it.id == id } ?: BuiltInWallpapers.first()
