package com.breez.notes.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.model.WidgetDisplayMode
import com.breez.notes.domain.model.WidgetSourceType
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.theme.BreezTheme
import com.breez.notes.ui.theme.PureWhite
import com.breez.notes.ui.theme.ScrimBlack
import com.breez.notes.ui.theme.Transparent
import com.breez.notes.ui.theme.WidgetTextPalette
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.toHex
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setResult(Activity.RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        setContent {
            BreezTheme {
                WidgetConfigRoute(
                    appWidgetId = appWidgetId,
                    onDone = {
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun WidgetConfigRoute(
    appWidgetId: Int,
    onDone: () -> Unit,
    viewModel: WidgetConfigViewModel = hiltViewModel()
) {
    LaunchedEffect(appWidgetId) { viewModel.bind(appWidgetId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val stored = WidgetBackgroundStore.persistOriginal(context, appWidgetId, uri)
        viewModel.setBackground(stored ?: uri.toString())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = { BreezTopBar(title = stringResource(R.string.widget_config_title), transparent = true) },
        bottomBar = {
            BreezButton(
                text = stringResource(R.string.widget_done),
                onClick = { viewModel.save(onDone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(stringResource(R.string.widget_size), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    R.string.widget_size_value,
                    state.config.cellWidth,
                    state.config.cellHeight
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.widget_size_width), style = MaterialTheme.typography.labelLarge)
            SizeStepRow(
                range = WidgetCells.MIN_WIDTH..WidgetCells.MAX_WIDTH,
                selected = state.config.cellWidth,
                onSelect = viewModel::setCellWidth
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.widget_size_height), style = MaterialTheme.typography.labelLarge)
            SizeStepRow(
                range = WidgetCells.MIN_HEIGHT..WidgetCells.MAX_HEIGHT,
                selected = state.config.cellHeight,
                onSelect = viewModel::setCellHeight
            )
            Spacer(Modifier.height(12.dp))
            BreezWidgetLivePreview(
                notes = state.previewNotes,
                config = state.config,
                emptyLabel = stringResource(R.string.widget_preview_empty),
                onPhotoTransform = viewModel::setPhotoTransform
            )
            if (!state.config.backgroundUri.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.widget_photo_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                BreezTextButton(
                    text = stringResource(R.string.widget_reset_photo),
                    onClick = viewModel::resetPhotoTransform
                )
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.widget_background), style = MaterialTheme.typography.titleMedium)
                BreezButton(
                    text = stringResource(R.string.widget_pick_background),
                    onClick = {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.widget_opacity))
                Slider(
                    value = state.config.backgroundOpacity,
                    onValueChange = viewModel::setOpacity,
                    valueRange = 0f..1f
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.widget_source), style = MaterialTheme.typography.titleMedium)
                SourceRow(
                    selected = state.config.sourceType == WidgetSourceType.ALL,
                    label = stringResource(R.string.widget_source_all),
                    onClick = { viewModel.setSource(WidgetSourceType.ALL) }
                )
                SourceRow(
                    selected = state.config.sourceType == WidgetSourceType.FOLDER,
                    label = stringResource(R.string.widget_source_folder),
                    onClick = { viewModel.setSource(WidgetSourceType.FOLDER) }
                )
                if (state.config.sourceType == WidgetSourceType.FOLDER) {
                    FolderDropdown(
                        folders = state.folders.map { it.id to it.name },
                        selectedId = state.config.folderId,
                        placeholder = stringResource(R.string.widget_select_folder),
                        onSelect = { viewModel.setFolder(it) }
                    )
                }
                SourceRow(
                    selected = state.config.sourceType == WidgetSourceType.NOTE,
                    label = stringResource(R.string.widget_source_note),
                    onClick = { viewModel.setSource(WidgetSourceType.NOTE) }
                )
                if (state.config.sourceType == WidgetSourceType.NOTE) {
                    FolderDropdown(
                        folders = state.notes.map { it.id to it.title.ifBlank { context.getString(R.string.note_untitled) } },
                        selectedId = state.config.noteId,
                        placeholder = stringResource(R.string.widget_select_note),
                        onSelect = { viewModel.setNote(it) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.widget_text_color), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WidgetTextPalette.forEach { color ->
                        val hex = color.toHex()
                        val selected = state.config.textColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                                .clickable { viewModel.setTextColor(hex) }
                        )
                    }
                }
                Text(stringResource(R.string.widget_font_size))
                Slider(
                    value = state.config.fontSizeSp.toFloat(),
                    onValueChange = { viewModel.setFontSize(it.toInt()) },
                    valueRange = 10f..24f,
                    steps = 13
                )
                Text(stringResource(R.string.widget_display_mode), style = MaterialTheme.typography.titleMedium)
                SourceRow(
                    selected = state.config.displayMode == WidgetDisplayMode.TITLE,
                    label = stringResource(R.string.widget_mode_title),
                    onClick = { viewModel.setDisplayMode(WidgetDisplayMode.TITLE) }
                )
                SourceRow(
                    selected = state.config.displayMode == WidgetDisplayMode.FULL,
                    label = stringResource(R.string.widget_mode_full),
                    onClick = { viewModel.setDisplayMode(WidgetDisplayMode.FULL) }
                )
                SourceRow(
                    selected = state.config.displayMode == WidgetDisplayMode.CHECKLIST,
                    label = stringResource(R.string.widget_mode_checklist),
                    onClick = { viewModel.setDisplayMode(WidgetDisplayMode.CHECKLIST) }
                )
                Text(stringResource(R.string.widget_max_notes))
                Slider(
                    value = state.config.maxNotes.toFloat(),
                    onValueChange = { viewModel.setMaxNotes(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Text(stringResource(R.string.widget_corner_radius))
                Slider(
                    value = state.config.cornerRadiusDp.toFloat(),
                    onValueChange = { viewModel.setCornerRadius(it.toInt()) },
                    valueRange = 0f..32f
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SizeStepRow(
    range: IntRange,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        range.forEach { value ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BreezWidgetLivePreview(
    notes: List<Note>,
    config: WidgetConfig,
    emptyLabel: String,
    onPhotoTransform: (Float, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textColor = parseHexColor(config.textColorHex, fallback = PureWhite)
    val shape = RoundedCornerShape(config.cornerRadiusDp.coerceIn(0, 32).dp)
    val cols = WidgetCells.coerceWidth(config.cellWidth)
    val rows = WidgetCells.coerceHeight(config.cellHeight)
    var bitmap by remember(config.backgroundUri, config.appWidgetId) { mutableStateOf<Bitmap?>(null) }
    val latestConfig by rememberUpdatedState(config)
    val latestBitmap by rememberUpdatedState(bitmap)
    val latestOnTransform by rememberUpdatedState(onPhotoTransform)
    LaunchedEffect(config.backgroundUri, config.appWidgetId) {
        bitmap = withContext(Dispatchers.IO) {
            WidgetBackgroundStore.loadOriginal(context, config.appWidgetId, config.backgroundUri)
        }
    }
    Column {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val cell = minOf(maxWidth / cols, 480.dp / rows)
        val previewWidth = cell * cols
        val previewHeight = cell * rows
        val viewWidthPx = with(density) { previewWidth.toPx() }
        val viewHeightPx = with(density) { previewHeight.toPx() }
        Box(
            modifier = Modifier
                .width(previewWidth)
                .height(previewHeight)
                .clip(shape)
                .clipToBounds()
                .then(
                    if (bitmap != null) {
                        Modifier.pointerInput(cols, rows, viewWidthPx, viewHeightPx) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val image = latestBitmap ?: return@detectTransformGestures
                                val current = latestConfig
                                val nextScale = WidgetImageTransform.coerceScale(current.backgroundScale * zoom)
                                val nextX = current.backgroundOffsetX + pan.x / viewWidthPx
                                val nextY = current.backgroundOffsetY + pan.y / viewHeightPx
                                val clamped = WidgetImageTransform.clampOffset(
                                    imageWidth = image.width.toFloat(),
                                    imageHeight = image.height.toFloat(),
                                    viewWidth = viewWidthPx,
                                    viewHeight = viewHeightPx,
                                    scale = nextScale,
                                    offsetXFrac = nextX,
                                    offsetYFrac = nextY
                                )
                                latestOnTransform(nextScale, clamped.first, clamped.second)
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            val photo = bitmap
            if (photo != null) {
                val layout = WidgetImageTransform.layout(
                    imageWidth = photo.width.toFloat(),
                    imageHeight = photo.height.toFloat(),
                    viewWidth = viewWidthPx,
                    viewHeight = viewHeightPx,
                    scale = config.backgroundScale,
                    offsetXFrac = config.backgroundOffsetX,
                    offsetYFrac = config.backgroundOffsetY
                )
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset { IntOffset(layout.left.roundToInt(), layout.top.roundToInt()) }
                        .size(
                            width = with(density) { layout.displayWidth.toDp() },
                            height = with(density) { layout.displayHeight.toDp() }
                        )
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(parseHexColor("#1A2332")))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScrimBlack.copy(alpha = config.backgroundOpacity.coerceIn(0f, 1f)))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                if (notes.isEmpty()) {
                    Text(text = emptyLabel, color = textColor, fontSize = config.fontSizeSp.sp)
                } else {
                    notes.take(config.maxNotes.coerceIn(1, 5)).forEach { note ->
                        PreviewNoteText(note = note, config = config, textColor = textColor)
                    }
                }
            }
        }
    }
        val photoForScale = bitmap
        if (photoForScale != null) {
            Text(
                text = stringResource(R.string.widget_photo_scale),
                modifier = Modifier.padding(top = 8.dp)
            )
            Slider(
                value = config.backgroundScale,
                onValueChange = { scale ->
                    val clamped = WidgetImageTransform.clampOffset(
                        imageWidth = photoForScale.width.toFloat(),
                        imageHeight = photoForScale.height.toFloat(),
                        viewWidth = cols.toFloat(),
                        viewHeight = rows.toFloat(),
                        scale = scale,
                        offsetXFrac = config.backgroundOffsetX,
                        offsetYFrac = config.backgroundOffsetY
                    )
                    onPhotoTransform(scale, clamped.first, clamped.second)
                },
                valueRange = WidgetImageTransform.MIN_SCALE..WidgetImageTransform.MAX_SCALE
            )
        }
    }
}

@Composable
private fun PreviewNoteText(
    note: Note,
    config: WidgetConfig,
    textColor: androidx.compose.ui.graphics.Color
) {
    val size = config.fontSizeSp.sp
    val title = note.title.ifBlank { "•" }
    when (config.displayMode) {
        WidgetDisplayMode.TITLE -> Text(title, color = textColor, fontSize = size, maxLines = 1, overflow = TextOverflow.Ellipsis)
        WidgetDisplayMode.FULL -> {
            Text(title, color = textColor, fontSize = size, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (note.body.isNotBlank()) {
                Text(note.body, color = textColor, fontSize = (config.fontSizeSp - 2).coerceAtLeast(10).sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        WidgetDisplayMode.CHECKLIST -> {
            WidgetChecklist.lines(note.body, title).take(4).forEach { line ->
                val mark = if (line.done) "☑  " else "☐  "
                Text(
                    text = mark + line.text,
                    color = textColor,
                    fontSize = size,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (line.done) TextDecoration.LineThrough else TextDecoration.None
                )
            }
        }
    }
}

@Composable
private fun SourceRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun FolderDropdown(
    folders: List<Pair<Long, String>>,
    selectedId: Long?,
    placeholder: String,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = folders.firstOrNull { it.first == selectedId }?.second ?: placeholder
    Box {
        BreezButton(text = label, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth())
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            folders.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
