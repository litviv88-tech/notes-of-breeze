package com.breez.notes.ui.theme

import androidx.compose.ui.graphics.Color

val BreezBlue = Color(0xFF4A90E2)
val Mint = Color(0xFF7ED9C4)
val Lavender = Color(0xFFA78BFA)
val Coral = Color(0xFFFF7A85)
val Sunset = Color(0xFFFFB347)
val Forest = Color(0xFF4ADE80)
val Ocean = Color(0xFF06B6D4)
val Rose = Color(0xFFF472B6)
val Amber = Color(0xFFFBBF24)
val Slate = Color(0xFF64748B)
val Cherry = Color(0xFFEF4444)
val Monochrome = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF7F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A2332)
val LightOnBackground = Color(0xFF1A2332)
val LightOutline = Color(0xFFD7DEE8)

val DarkBackground = Color(0xFF10151C)
val DarkSurface = Color(0xFF1A2332)
val DarkOnSurface = Color(0xFFF2F5F8)
val DarkOnBackground = Color(0xFFF2F5F8)
val DarkOutline = Color(0xFF3A4658)

val ScrimBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)
val Transparent = Color(0x00000000)
val CardScrim = Color(0x99000000)
val NoteCardFillLight = Color(0xE6FFFFFF)
val NoteCardFillDark = Color(0xCC1A2332)
val PinGold = Color(0xFFFBBF24)
val ErrorRed = Color(0xFFEF4444)
val DismissRed = Color(0xFFB91C1C)

val WidgetTextPalette = listOf(
    Color(0xFFFFFFFF),
    Color(0xFF1A2332),
    Color(0xFF4A90E2),
    Color(0xFF7ED9C4),
    Color(0xFFFBBF24),
    Color(0xFFFF7A85),
    Color(0xFFA78BFA),
    Color(0xFFF2F5F8)
)

fun parseHexColor(hex: String, fallback: Color = BreezBlue): Color {
    val normalized = if (hex.startsWith("#")) hex else "#$hex"
    return try {
        Color(android.graphics.Color.parseColor(normalized))
    } catch (_: IllegalArgumentException) {
        fallback
    }
}

fun Color.toHex(): String {
    val argb = android.graphics.Color.argb(
        (alpha * 255).toInt().coerceIn(0, 255),
        (red * 255).toInt().coerceIn(0, 255),
        (green * 255).toInt().coerceIn(0, 255),
        (blue * 255).toInt().coerceIn(0, 255)
    )
    return String.format("#%06X", 0xFFFFFF and argb)
}
