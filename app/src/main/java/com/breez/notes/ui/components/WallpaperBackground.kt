package com.breez.notes.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.model.WallpaperType
import com.breez.notes.ui.media.LoopingTextureVideo
import com.breez.notes.ui.theme.ScrimBlack
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.wallpaperById
import java.io.File

@Composable
fun WallpaperBackground(
    settings: WallpaperSettings,
    modifier: Modifier = Modifier
) {
    val blurPx = with(LocalDensity.current) { settings.wallpaperBlur.coerceIn(0f, 25f).dp.toPx() }
    val expand = if (blurPx > 0.1f) 1.2f else 1f
    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = expand
                    scaleY = expand
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurPx > 0.1f) {
                        renderEffect = RenderEffect
                            .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                }
        ) {
            when (settings.wallpaperType) {
                WallpaperType.BUILTIN -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(wallpaperById(settings.builtInWallpaperId).brush)
                    )
                }
                WallpaperType.PHOTO -> {
                    val uri = settings.wallpaperUri
                    if (uri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(wallpaperById(settings.builtInWallpaperId).brush)
                        )
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                WallpaperType.COLOR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(parseHexColor(settings.wallpaperColorHex ?: "#4A90E2"))
                    )
                }
                WallpaperType.VIDEO -> {
                    val file = settings.wallpaperUri?.let(::File)
                    if (file != null && file.exists()) {
                        LoopingTextureVideo(
                            file = file,
                            modifier = Modifier.fillMaxSize(),
                            muted = true
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(wallpaperById(settings.builtInWallpaperId).brush)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimBlack.copy(alpha = settings.wallpaperDim.coerceIn(0f, 0.8f)))
        )
    }
}
