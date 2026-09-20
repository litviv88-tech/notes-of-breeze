package com.breez.notes.ui.media

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

@Composable
fun LoopingTextureVideo(
    file: File,
    modifier: Modifier = Modifier,
    muted: Boolean = true
) {
    LoopingTextureVideo(
        sourceKey = file.absolutePath,
        modifier = modifier,
        muted = muted,
        bind = { player, _ -> player.setDataSource(file.absolutePath) }
    )
}

@Composable
fun LoopingTextureVideo(
    uri: Uri,
    modifier: Modifier = Modifier,
    muted: Boolean = true
) {
    val context = LocalContext.current
    LoopingTextureVideo(
        sourceKey = uri.toString(),
        modifier = modifier,
        muted = muted,
        bind = { player, _ ->
            if (uri.scheme == "file") {
                player.setDataSource(uri.path)
            } else {
                player.setDataSource(context, uri)
            }
        }
    )
}

@Composable
private fun LoopingTextureVideo(
    sourceKey: String,
    modifier: Modifier,
    muted: Boolean,
    bind: (MediaPlayer, Context) -> Unit
) {
    var player by remember(sourceKey) { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(sourceKey) {
        onDispose {
            runCatching { player?.stop() }
            runCatching { player?.release() }
            player = null
        }
    }
    key(sourceKey) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                TextureView(context).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            texture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val mediaPlayer = MediaPlayer()
                            player = mediaPlayer
                            runCatching {
                                bind(mediaPlayer, context)
                                mediaPlayer.setSurface(Surface(texture))
                                mediaPlayer.isLooping = true
                                if (muted) mediaPlayer.setVolume(0f, 0f)
                                mediaPlayer.setOnErrorListener { _, _, _ -> true }
                                mediaPlayer.setOnVideoSizeChangedListener { prepared, _, _ ->
                                    applyCenterCrop(this@apply, prepared)
                                }
                                mediaPlayer.setOnPreparedListener { prepared ->
                                    if (muted) prepared.setVolume(0f, 0f)
                                    applyCenterCrop(this@apply, prepared)
                                    prepared.start()
                                }
                                mediaPlayer.prepareAsync()
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surface: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            player?.let { applyCenterCrop(this@apply, it) }
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            runCatching { player?.stop() }
                            runCatching { player?.release() }
                            player = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
                }
            }
        )
    }
}

private fun applyCenterCrop(view: TextureView, player: MediaPlayer) {
    val videoW = player.videoWidth
    val videoH = player.videoHeight
    if (videoW <= 0 || videoH <= 0 || view.width <= 0 || view.height <= 0) return
    val viewW = view.width.toFloat()
    val viewH = view.height.toFloat()
    val scale = maxOf(viewW / videoW, viewH / videoH)
    val matrix = Matrix()
    matrix.setScale(
        scale * videoW / viewW,
        scale * videoH / viewH,
        viewW / 2f,
        viewH / 2f
    )
    view.setTransform(matrix)
}
