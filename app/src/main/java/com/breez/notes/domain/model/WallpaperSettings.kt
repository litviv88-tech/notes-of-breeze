package com.breez.notes.domain.model

enum class WallpaperType {
    BUILTIN,
    PHOTO,
    COLOR,
    VIDEO
}

data class WallpaperSettings(
    val wallpaperType: WallpaperType = WallpaperType.BUILTIN,
    val builtInWallpaperId: String = "breeze_sky",
    val wallpaperUri: String? = null,
    val wallpaperUriDark: String? = null,
    val wallpaperColorHex: String? = "#4A90E2",
    val wallpaperDim: Float = 0.25f,
    val wallpaperBlur: Float = 0f
) {
    companion object {
        const val MAX_VIDEO_DURATION_MS = 30_000L
    }
}
