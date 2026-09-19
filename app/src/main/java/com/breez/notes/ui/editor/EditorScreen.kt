package com.breez.notes.ui.editor

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.breez.notes.R
import com.breez.notes.domain.model.ChecklistFormat
import com.breez.notes.domain.model.ChecklistItem
import com.breez.notes.domain.model.Folder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.components.ColorPickerDialog
import com.breez.notes.ui.folders.FolderMarkBadge
import com.breez.notes.ui.media.VideoTrimDialog
import com.breez.notes.ui.theme.Transparent
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.toHex
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }
    var colorPicker by remember { mutableStateOf(false) }
    var datePicker by remember { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<Long?>(null) }
    var deleteConfirm by remember { mutableStateOf(false) }
    var pendingLocationAction by remember { mutableStateOf<LocationAction?>(null) }
    var trimUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.addAttachment(it, "image/*") }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { trimUri = it }
    }
    val backgroundPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val ok = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        when (pendingLocationAction) {
            LocationAction.Current -> if (ok) viewModel.useCurrentLocation()
            LocationAction.Nearby -> if (ok) {
                viewModel.onLocationReminderChange(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            null -> Unit
        }
        pendingLocationAction = null
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        viewModel.clearMessage()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.save()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.save()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BreezTopBar(
                title = if (state.isChecklist) {
                    if (state.isNew) stringResource(R.string.todo_editor_title) else stringResource(R.string.editor_save)
                } else {
                    if (state.isNew) stringResource(R.string.note_add) else stringResource(R.string.editor_save)
                },
                onBack = {
                    scope.launch {
                        viewModel.persist()
                        onBack()
                    }
                },
                transparent = true,
                actions = {
                    BreezTextButton(
                        text = stringResource(R.string.editor_save),
                        onClick = {
                            scope.launch {
                                viewModel.persist()
                                onBack()
                            }
                        }
                    )
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.editor_menu))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_folder)) },
                            onClick = {
                                menuOpen = false
                                sheetOpen = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_color)) },
                            onClick = {
                                menuOpen = false
                                colorPicker = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_reminder)) },
                            onClick = {
                                menuOpen = false
                                datePicker = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (state.isPinned) stringResource(R.string.note_unpin)
                                    else stringResource(R.string.note_pin)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                viewModel.onPinnedChange(!state.isPinned)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_delete)) },
                            onClick = {
                                menuOpen = false
                                deleteConfirm = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BreezTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                hint = stringResource(R.string.editor_title_hint),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))
            if (state.isChecklist) {
                ChecklistEditor(
                    items = ChecklistFormat.parse(state.body),
                    onChange = viewModel::onChecklistItemsChange
                )
            } else {
                BreezTextField(
                    value = state.body,
                    onValueChange = viewModel::onBodyChange,
                    hint = stringResource(R.string.editor_body_hint),
                    modifier = Modifier.height(220.dp)
                )
            }
            if (!state.isChecklist) {
                Spacer(Modifier.height(16.dp))
                AttachmentSection(
                    attachments = state.attachments,
                    busy = state.busy,
                    onAddPhoto = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddVideo = {
                        videoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onRemove = viewModel::removeAttachment
                )
                Spacer(Modifier.height(16.dp))
                MeetingPlaceSection(
                    place = state.meetingPlace,
                    latitude = state.meetingLat,
                    longitude = state.meetingLng,
                    locationReminder = state.locationReminder,
                    onPlaceChange = viewModel::onMeetingPlaceChange,
                    onResolve = viewModel::resolveMeetingPlace,
                    onUseCurrent = {
                        pendingLocationAction = LocationAction.Current
                        locationPermission.launch(locationPermissions())
                    },
                    onLocationReminderChange = { enabled ->
                        if (enabled) {
                            pendingLocationAction = LocationAction.Nearby
                            locationPermission.launch(locationPermissions())
                        } else {
                            viewModel.onLocationReminderChange(false)
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
            BreezButton(
                text = stringResource(R.string.editor_folder) + " / " + stringResource(R.string.editor_color) + " / " + stringResource(R.string.editor_reminder),
                onClick = { sheetOpen = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }, sheetState = sheetState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.editor_folder), style = MaterialTheme.typography.titleMedium)
                FolderOption(
                    name = stringResource(R.string.editor_no_folder),
                    selected = state.folderId == null,
                    onClick = { viewModel.onFolderChange(null) }
                )
                state.folders.forEach { folder ->
                    FolderOption(
                        name = folder.name,
                        selected = state.folderId == folder.id,
                        onClick = { viewModel.onFolderChange(folder.id) },
                        folder = folder
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.editor_color), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(state.colorHex))
                        .clickable { colorPicker = true }
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.editor_reminder), style = MaterialTheme.typography.titleMedium)
                val reminderLabel = if (state.reminderAt == null) {
                    stringResource(R.string.editor_reminder_none)
                } else {
                    stringResource(R.string.editor_reminder_set)
                }
                BreezTextButton(text = reminderLabel, onClick = { datePicker = true })
                if (state.reminderAt != null) {
                    BreezTextButton(
                        text = stringResource(R.string.editor_reminder_clear),
                        onClick = { viewModel.onReminderChange(null) }
                    )
                    RecurrenceSection(
                        recurrence = state.recurrence,
                        onChange = viewModel::onRecurrenceChange
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (colorPicker) {
        ColorPickerDialog(
            initialColor = parseHexColor(state.colorHex),
            onColorSelected = { viewModel.onColorChange(it.toHex()) },
            onDismiss = { colorPicker = false }
        )
    }

    val pendingTrim = trimUri
    if (pendingTrim != null) {
        VideoTrimDialog(
            source = pendingTrim,
            maxDurationMs = Long.MAX_VALUE,
            hint = stringResource(R.string.video_trim_hint_note),
            onConfirm = { file ->
                viewModel.addAttachment(Uri.fromFile(file), "video/mp4")
                trimUri = null
            },
            onDismiss = { trimUri = null }
        )
    }

    if (datePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { datePicker = false },
            confirmButton = {
                BreezTextButton(
                    text = stringResource(R.string.editor_confirm),
                    onClick = {
                        pendingDate = dateState.selectedDateMillis
                        datePicker = false
                        timePicker = true
                    }
                )
            },
            dismissButton = {
                BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = { datePicker = false })
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (timePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { timePicker = false },
            confirmButton = {
                BreezButton(
                    text = stringResource(R.string.editor_confirm),
                    onClick = {
                        val date = pendingDate ?: System.currentTimeMillis()
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = date
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        viewModel.onReminderChange(calendar.timeInMillis)
                        timePicker = false
                    }
                )
            },
            dismissButton = {
                BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = { timePicker = false })
            },
            title = { Text(stringResource(R.string.editor_pick_time)) },
            text = { TimePicker(state = timeState) }
        )
    }

    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            confirmButton = {
                BreezButton(
                    text = stringResource(R.string.editor_delete),
                    onClick = { viewModel.delete { onBack() } }
                )
            },
            dismissButton = {
                BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = { deleteConfirm = false })
            },
            title = { Text(stringResource(R.string.editor_delete_confirm)) }
        )
    }
}

private enum class LocationAction { Current, Nearby }

@Composable
private fun ChecklistEditor(
    items: List<ChecklistItem>,
    onChange: (List<ChecklistItem>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.done,
                    onCheckedChange = { checked ->
                        onChange(items.toMutableList().also { it[index] = item.copy(done = checked) })
                    }
                )
                BreezTextField(
                    value = item.text,
                    onValueChange = { text ->
                        onChange(items.toMutableList().also { it[index] = item.copy(text = text) })
                    },
                    hint = stringResource(R.string.todo_item_hint),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (item.done) 0.5f else 1f)
                    )
                )
            }
        }
        BreezTextButton(
            text = stringResource(R.string.todo_add_item),
            onClick = { onChange(items + ChecklistItem("")) }
        )
    }
}

private fun locationPermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
}.toTypedArray()

@Composable
private fun FolderOption(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    folder: Folder? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        RadioButton(selected = selected, onClick = onClick)
        if (folder != null) {
            FolderMarkBadge(folder = folder, size = 24.dp)
            Spacer(Modifier.size(8.dp))
        }
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}
