package com.breez.notes.ui.editor

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.ChecklistItem
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Тайминги анимаций списка дел (как CSS-переменные / JS-константы).
 * Меняйте значения здесь — все строки используют эти константы.
 *
 * Фазы:
 * 1) CHECK_MS     — «рисование» галочки (аналог SVG stroke-dashoffset)
 * 2) STRIKE_MS    — зачёркивание и затемнение текста (идёт параллельно с CHECK)
 * 3) HOLD_MS      — пауза в зачёркнутом виде (~6 с, коридор 5–7 с)
 * 4) FADE_MS      — blur + opacity → 0 (~1 с)
 * 5) COLLAPSE_MS  — схлопывание высоты, нижние пункты поднимаются вверх
 */
object TodoAnimTiming {
    /** Длительность отрисовки галочки. */
    const val CHECK_MS = 320

    /** Длительность зачёркивания / затемнения текста. */
    const val STRIKE_MS = 320

    /** Пауза перед удалением выполненной задачи. */
    const val HOLD_MS = 6_000L

    /** Растворение: opacity + blur. */
    const val FADE_MS = 1_000

    /** Схлопывание высоты (layout shift без рывков). */
    const val COLLAPSE_MS = 420

    /** Итоговая прозрачность выполненного текста (0.4–0.5). */
    const val DONE_TEXT_ALPHA = 0.45f

    /** Максимальный blur при удалении (dp). На API &lt; 31 blur пропускается. */
    const val MAX_BLUR_DP = 8f
}

private data class TodoRow(
    val id: Long,
    val text: String,
    val done: Boolean,
    /** Автоудаление только после отметки в этой сессии (не для уже сохранённых [x]). */
    val pendingRemoval: Boolean = false,
    val visible: Boolean = true
)

@Composable
fun AnimatedChecklistEditor(
    items: List<ChecklistItem>,
    onChange: (List<ChecklistItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    var nextId by remember { mutableLongStateOf(1L) }
    val rows = remember {
        mutableStateListOf<TodoRow>().also { list ->
            val seed = items.ifEmpty { listOf(ChecklistItem("")) }
            seed.forEach { item ->
                list.add(
                    TodoRow(
                        id = nextId,
                        text = item.text,
                        done = item.done
                    )
                )
                nextId += 1
            }
        }
    }

    fun publishVisible() {
        val published = rows
            .filter { it.visible }
            .map { ChecklistItem(it.text, it.done) }
        onChange(published.ifEmpty { listOf(ChecklistItem("")) })
    }

    fun updateRow(id: Long, transform: (TodoRow) -> TodoRow) {
        val index = rows.indexOfFirst { it.id == id }
        if (index >= 0) rows[index] = transform(rows[index])
    }

    fun ensureNotEmpty() {
        if (rows.none { it.visible }) {
            rows.add(TodoRow(id = nextId++, text = "", done = false))
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { row ->
            key(row.id) {
                AnimatedVisibility(
                    visible = row.visible,
                    enter = fadeIn(tween(TodoAnimTiming.COLLAPSE_MS, easing = FastOutSlowInEasing)) +
                        expandVertically(
                            animationSpec = tween(
                                TodoAnimTiming.COLLAPSE_MS,
                                easing = FastOutSlowInEasing
                            )
                        ),
                    // Fade уже делает exitProgress; здесь только плавное схлопывание высоты.
                    exit = shrinkVertically(
                        animationSpec = tween(
                            TodoAnimTiming.COLLAPSE_MS,
                            easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    AnimatedTodoRow(
                        row = row,
                        canDeleteManually = rows.count { it.visible } > 1,
                        onTextChange = { text ->
                            updateRow(row.id) { it.copy(text = text) }
                            publishVisible()
                        },
                        onToggle = { checked ->
                            updateRow(row.id) {
                                if (checked) {
                                    it.copy(done = true, pendingRemoval = true)
                                } else {
                                    it.copy(done = false, pendingRemoval = false)
                                }
                            }
                            publishVisible()
                        },
                        onHideForExit = {
                            updateRow(row.id) { it.copy(visible = false) }
                        },
                        onRemovalFinished = {
                            rows.removeAll { it.id == row.id }
                            ensureNotEmpty()
                            publishVisible()
                        },
                        onManualDelete = {
                            rows.removeAll { it.id == row.id }
                            ensureNotEmpty()
                            publishVisible()
                        }
                    )
                }
            }
        }

        BreezTextButton(
            text = stringResource(R.string.todo_add_item),
            onClick = {
                rows.add(TodoRow(id = nextId++, text = "", done = false))
                publishVisible()
            }
        )
    }
}

@Composable
private fun AnimatedTodoRow(
    row: TodoRow,
    canDeleteManually: Boolean,
    onTextChange: (String) -> Unit,
    onToggle: (Boolean) -> Unit,
    onHideForExit: () -> Unit,
    onRemovalFinished: () -> Unit,
    onManualDelete: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val completeProgress = remember {
        Animatable(if (row.done && !row.pendingRemoval) 1f else 0f)
    }
    val exitProgress = remember { Animatable(0f) }

    LaunchedEffect(row.pendingRemoval, row.done) {
        if (row.pendingRemoval && row.done) {
            // Фаза 1 — галочка и зачёркивание (ease-in-out ≈ FastOutSlowIn)
            completeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(TodoAnimTiming.CHECK_MS, easing = FastOutSlowInEasing)
            )
            // Фаза 2 — удержание
            delay(TodoAnimTiming.HOLD_MS)
            // Фаза 3 — растворение (blur + opacity)
            launch {
                exitProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(TodoAnimTiming.FADE_MS, easing = FastOutSlowInEasing)
                )
            }
            // На середине fade запускаем схлопывание высоты — нижние пункты едут вверх без рывка.
            delay(TodoAnimTiming.FADE_MS.toLong() / 2)
            onHideForExit()
            delay(TodoAnimTiming.COLLAPSE_MS.toLong() + TodoAnimTiming.FADE_MS.toLong() / 2)
            onRemovalFinished()
        } else if (!row.done) {
            // Снятие галочки: отмена удаления и обратная анимация
            exitProgress.snapTo(0f)
            completeProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(TodoAnimTiming.CHECK_MS, easing = FastOutSlowInEasing)
            )
        } else {
            // Уже выполненный пункт из сохранённой заметки — без автоудаления
            completeProgress.snapTo(1f)
            exitProgress.snapTo(0f)
        }
    }

    val density = LocalDensity.current
    val blurRadius = with(density) {
        (TodoAnimTiming.MAX_BLUR_DP * exitProgress.value).dp
    }
    val rowAlpha = 1f - exitProgress.value
    // Параллельно с галочкой текст светлеет (STRIKE_MS ≈ CHECK_MS)
    val textAlpha = 1f - completeProgress.value * (1f - TodoAnimTiming.DONE_TEXT_ALPHA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && exitProgress.value > 0f) {
                    Modifier.blur(blurRadius)
                } else {
                    Modifier
                }
            )
            .graphicsLayer { alpha = rowAlpha }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TodoCircleCheckbox(
            progress = completeProgress.value,
            color = contentColor,
            // Не больше высоты заглавной буквы bodyLarge (~16sp)
            size = 16.dp,
            onClick = { onToggle(!row.done) }
        )

        BreezTextField(
            value = row.text,
            onValueChange = onTextChange,
            hint = stringResource(R.string.todo_item_hint),
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = contentColor.copy(alpha = textAlpha),
                textDecoration = if (completeProgress.value > 0.15f) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            )
        )

        if (canDeleteManually) {
            BreezTextButton(text = "×", onClick = onManualDelete)
        }
    }
}

/**
 * Круглый чекбокс: цвет обводки = цвет текста.
 * Галочка рисуется через PathMeasure — аналог SVG stroke-dashoffset.
 */
@Composable
private fun TodoCircleCheckbox(
    progress: Float,
    color: Color,
    size: Dp,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Checkbox,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(
                width = size.toPx() * 0.12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = (this.size.minDimension / 2f) - stroke.width,
                style = stroke
            )
            if (progress > 0f) {
                val w = this.size.width
                val h = this.size.height
                val check = Path().apply {
                    moveTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.72f)
                    lineTo(w * 0.78f, h * 0.28f)
                }
                val measure = PathMeasure()
                measure.setPath(check, false)
                val drawn = Path()
                measure.getSegment(
                    startDistance = 0f,
                    stopDistance = measure.length * progress.coerceIn(0f, 1f),
                    destination = drawn,
                    startWithMoveTo = true
                )
                drawPath(path = drawn, color = color, style = stroke)
            }
        }
    }
}
