package com.breez.notes.ui.reorder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReorderHandle(
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.DragIndicator,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
    }
}

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
