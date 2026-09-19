package com.breez.notes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.domain.model.Palette
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.ui.components.WallpaperBackground
import com.breez.notes.ui.settings.ThemeViewModel
import java.util.Calendar

fun isAutoDarkNow(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour >= 20 || hour < 7
}

@Composable
fun BreezTheme(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val settings by themeViewModel.settings.collectAsStateWithLifecycle()
    val wallpaper by themeViewModel.wallpaper.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val darkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.AUTO -> isAutoDarkNow()
    }
    val colorScheme = when {
        settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        settings.paletteId == Palette.CUSTOM_ID -> {
            val hex = settings.customColorHex ?: "#4A90E2"
            buildColorScheme(hex, darkTheme)
        }
        else -> {
            val palette = themeViewModel.paletteFor(settings.paletteId)
            buildColorScheme(palette.primaryHex, darkTheme)
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperBackground(
                settings = wallpaper,
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize()) {
                content()
            }
        }
    }
}
