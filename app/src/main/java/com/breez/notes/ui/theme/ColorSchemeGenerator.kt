package com.breez.notes.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class HslColor(
    val hue: Float,
    val saturation: Float,
    val lightness: Float
)

fun Color.toHsl(): HslColor {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val lightness = (max + min) / 2f
    val delta = max - min
    val saturation = if (delta == 0f) {
        0f
    } else {
        delta / (1f - abs(2f * lightness - 1f)).coerceAtLeast(0.0001f)
    }
    val hue = when {
        delta == 0f -> 0f
        max == red -> ((green - blue) / delta) % 6f
        max == green -> ((blue - red) / delta) + 2f
        else -> ((red - green) / delta) + 4f
    } * 60f
    return HslColor((hue + 360f) % 360f, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))
}

fun HslColor.toColor(): Color {
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f
    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}

fun buildColorScheme(hex: String, dark: Boolean): ColorScheme {
    val base = parseHexColor(hex)
    val hsl = base.toHsl()
    val primary = if (dark) {
        hsl.copy(lightness = (hsl.lightness + 0.15f).coerceIn(0f, 1f)).toColor()
    } else {
        base
    }
    val secondary = hsl.copy(hue = (hsl.hue + 30f) % 360f).toColor()
    val tertiary = hsl.copy(hue = (hsl.hue - 30f + 360f) % 360f).toColor()
    return if (dark) {
        darkColorScheme(
            primary = primary,
            onPrimary = DarkBackground,
            primaryContainer = primary.copy(alpha = 0.24f),
            onPrimaryContainer = DarkOnSurface,
            secondary = secondary,
            onSecondary = DarkBackground,
            tertiary = tertiary,
            onTertiary = DarkBackground,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurface,
            onSurfaceVariant = DarkOnSurface.copy(alpha = 0.8f),
            outline = DarkOutline,
            error = ErrorRed
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = PureWhite,
            primaryContainer = primary.copy(alpha = 0.16f),
            onPrimaryContainer = LightOnSurface,
            secondary = secondary,
            onSecondary = PureWhite,
            tertiary = tertiary,
            onTertiary = PureWhite,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightBackground,
            onSurfaceVariant = LightOnSurface.copy(alpha = 0.75f),
            outline = LightOutline,
            error = ErrorRed
        )
    }
}
