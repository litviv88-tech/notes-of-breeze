package com.breez.notes.ui.reorder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp

internal const val HOLD_TO_REORDER_MS = 8_000L

@Composable
fun ReorderHandle(
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    val current = LocalViewConfiguration.current
    val holdConfig = remember(current) {
        object : ViewConfiguration by current {
            override val longPressTimeoutMillis: Long = HOLD_TO_REORDER_MS
        }
    }
    var pressed by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(pressed) {
        if (pressed) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(HOLD_TO_REORDER_MS.toInt(), easing = LinearEasing)
            )
        } else {
            progress.snapTo(0f)
        }
    }
    CompositionLocalProvider(LocalViewConfiguration provides holdConfig) {
        Box(
            modifier = modifier
                .size(40.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        pressed = true
                        try {
                            waitForUpOrCancellation()
                        } finally {
                            pressed = false
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (pressed || progress.value > 0.01f) {
                CircularProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
            }
            Icon(
                imageVector = Icons.Rounded.DragIndicator,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
