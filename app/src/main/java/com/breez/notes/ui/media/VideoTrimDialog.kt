package com.breez.notes.ui.media

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.breez.notes.R
import com.breez.notes.data.files.VideoClipper
import com.breez.notes.data.files.formatVideoTime
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.theme.Transparent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimDialog(
    source: Uri,
    maxDurationMs: Long,
    hint: String,
    onConfirm: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipper = remember { VideoClipper(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var durationMs by remember(source) { mutableStateOf(0L) }
    var range by remember(source) { mutableStateOf(0f..0f) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    val failText = stringResource(R.string.video_trim_failed)

    LaunchedEffect(source) {
        val duration = withContext(Dispatchers.IO) { clipper.durationMs(source) }
        durationMs = duration
        val span = min(maxDurationMs, duration.coerceAtLeast(VideoClipper.MIN_CLIP_MS)).toFloat()
        range = 0f..span
    }

    LaunchedEffect(range, videoView) {
        val view = videoView ?: return@LaunchedEffect
        while (isActive) {
            if (view.isPlaying && view.currentPosition >= range.endInclusive.toInt()) {
                view.seekTo(range.start.toInt())
                view.start()
            }
            delay(160)
        }
    }

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !busy,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                BreezTopBar(
                    title = stringResource(R.string.video_trim_title),
                    onBack = if (busy) null else onDismiss
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setOnPreparedListener { player ->
                                    player.isLooping = false
                                    player.setVolume(0.4f, 0.4f)
                                    seekTo(range.start.toInt())
                                    start()
                                }
                                setVideoURI(source)
                                videoView = this
                            }
                        },
                        update = { view ->
                            if (view.tag != source.toString()) {
                                view.tag = source.toString()
                                view.setVideoURI(source)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(hint, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                if (durationMs > 0L) {
                    val selected = (range.endInclusive - range.start).toLong()
                    Text(
                        text = stringResource(
                            R.string.video_trim_range,
                            formatVideoTime(range.start.toLong()),
                            formatVideoTime(range.endInclusive.toLong()),
                            formatVideoTime(selected)
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    RangeSlider(
                        value = range,
                        onValueChange = { next ->
                            val maxSpan = min(maxDurationMs, durationMs).toFloat()
                            range = clampRange(next, range, maxSpan, durationMs.toFloat())
                            videoView?.seekTo(range.start.toInt())
                        },
                        valueRange = 0f..durationMs.toFloat()
                    )
                }
                if (!error.isNullOrBlank()) {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.weight(1f))
                BreezButton(
                    text = stringResource(R.string.video_trim_confirm),
                    enabled = !busy && durationMs > 0L,
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                runCatching {
                                    clipper.clip(
                                        source = source,
                                        startMs = range.start.toLong(),
                                        endMs = range.endInclusive.toLong()
                                    )
                                }
                            }
                            busy = false
                            result.onSuccess(onConfirm).onFailure {
                                error = failText
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (busy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.video_trim_busy))
                }
            }
        }
    }
}

private fun clampRange(
    next: ClosedFloatingPointRange<Float>,
    previous: ClosedFloatingPointRange<Float>,
    maxSpan: Float,
    duration: Float
): ClosedFloatingPointRange<Float> {
    var start = next.start.coerceIn(0f, duration)
    var end = next.endInclusive.coerceIn(0f, duration)
    if (end < start + VideoClipper.MIN_CLIP_MS) {
        end = (start + VideoClipper.MIN_CLIP_MS).coerceAtMost(duration)
        start = (end - VideoClipper.MIN_CLIP_MS).coerceAtLeast(0f)
    }
    if (end - start > maxSpan) {
        val movedStart = abs(start - previous.start) >= abs(end - previous.endInclusive)
        if (movedStart) {
            end = (start + maxSpan).coerceAtMost(duration)
            start = (end - maxSpan).coerceAtLeast(0f)
        } else {
            start = (end - maxSpan).coerceAtLeast(0f)
            end = (start + maxSpan).coerceAtMost(duration)
        }
    }
    return start..end
}
