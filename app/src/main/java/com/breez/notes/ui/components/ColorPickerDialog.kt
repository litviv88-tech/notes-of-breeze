package com.breez.notes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.ui.theme.PureBlack
import com.breez.notes.ui.theme.PureWhite
import com.breez.notes.ui.theme.Transparent
import com.breez.notes.ui.theme.toHex

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = remember(initialColor) {
        val array = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), array)
        array
    }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    val current = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BreezButton(
                text = stringResource(R.string.color_picker_apply),
                onClick = {
                    onColorSelected(current)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = onDismiss)
        },
        title = { Text(stringResource(R.string.color_picker_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SaturationValueSquare(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onChange = { s, v ->
                        saturation = s
                        value = v
                    }
                )
                Spacer(Modifier.height(16.dp))
                HueBar(hue = hue, onHueChange = { hue = it })
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(current)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                Text(text = current.toHex(), style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@Composable
private fun SaturationValueSquare(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit
) {
    val hueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    Canvas(
        modifier = Modifier
            .size(240.dp)
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    onChange(
                        (offset.x / size.width).coerceIn(0f, 1f),
                        (1f - offset.y / size.height).coerceIn(0f, 1f)
                    )
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onChange(
                        (change.position.x / size.width).coerceIn(0f, 1f),
                        (1f - change.position.y / size.height).coerceIn(0f, 1f)
                    )
                }
            }
    ) {
        drawRect(brush = Brush.horizontalGradient(listOf(PureWhite, hueColor)))
        drawRect(brush = Brush.verticalGradient(listOf(Transparent, PureBlack)))
        val thumb = Offset(saturation * size.width, (1f - value) * size.height)
        drawCircle(color = PureWhite, radius = 10f, center = thumb, style = Stroke(width = 4f))
        drawCircle(color = PureBlack, radius = 10f, center = thumb, style = Stroke(width = 1.5f))
    }
}

@Composable
private fun HueBar(hue: Float, onHueChange: (Float) -> Unit) {
    val colors = remember {
        listOf(
            Color(android.graphics.Color.HSVToColor(floatArrayOf(0f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(60f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(120f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(180f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(240f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(300f, 1f, 1f))),
            Color(android.graphics.Color.HSVToColor(floatArrayOf(360f, 1f, 1f)))
        )
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onHueChange((offset.x / size.width * 360f).coerceIn(0f, 360f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onHueChange((change.position.x / size.width * 360f).coerceIn(0f, 360f))
                }
            }
    ) {
        drawRect(brush = Brush.horizontalGradient(colors))
        val x = (hue / 360f) * size.width
        drawCircle(color = PureWhite, radius = 14f, center = Offset(x, size.height / 2f), style = Stroke(width = 4f))
    }
}
