package com.breez.notes.ui.folders

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField
import com.breez.notes.ui.components.ColorPickerDialog
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FolderEditDialog(
    visible: Boolean,
    existing: Folder?,
    error: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        colorHex: String,
        markType: FolderMarkType,
        mediaUri: Uri?,
        mimeType: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf(Folder.DEFAULT_FOLDER_COLOR) }
    var markType by remember { mutableStateOf(FolderMarkType.COLOR) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var mimeType by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var colorPicker by remember { mutableStateOf(false) }
    val shownError = error ?: localError
    val videoTooLong = stringResource(R.string.folder_video_too_long)

    LaunchedEffect(visible, existing?.id) {
        if (!visible) return@LaunchedEffect
        name = existing?.name.orEmpty()
        colorHex = existing?.colorHex ?: Folder.DEFAULT_FOLDER_COLOR
        markType = existing?.markType ?: FolderMarkType.COLOR
        previewUri = null
        mimeType = null
        localError = null
        colorPicker = false
        onClearError()
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        markType = FolderMarkType.PHOTO
        previewUri = uri
        mimeType = context.contentResolver.getType(uri) ?: "image/*"
        localError = null
        onClearError()
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val duration = withContext(Dispatchers.IO) { videoDurationMs(context, uri) }
            if (duration > Folder.MAX_VIDEO_DURATION_MS) {
                localError = videoTooLong
            } else {
                markType = FolderMarkType.VIDEO
                previewUri = uri
                mimeType = context.contentResolver.getType(uri) ?: "video/*"
                localError = null
                onClearError()
            }
        }
    }

    if (!visible) return

    val canSave = name.isNotBlank() && (
        markType == FolderMarkType.COLOR ||
            previewUri != null ||
            (existing != null && existing.markType == markType && existing.markFileName.isNotBlank())
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BreezButton(
                text = stringResource(if (existing == null) R.string.folder_create else R.string.folder_save),
                enabled = canSave,
                onClick = {
                    onSave(name, colorHex, markType, previewUri, mimeType)
                }
            )
        },
        dismissButton = {
            BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = onDismiss)
        },
        title = {
            Text(stringResource(if (existing == null) R.string.folder_add else R.string.folder_rename))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                BreezTextField(
                    value = name,
                    onValueChange = { name = it },
                    hint = stringResource(R.string.folder_name_hint),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.folder_mark), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = markType == FolderMarkType.COLOR,
                        onClick = {
                            markType = FolderMarkType.COLOR
                            previewUri = null
                            mimeType = null
                            localError = null
                        },
                        label = { Text(stringResource(R.string.folder_color)) }
                    )
                    FilterChip(
                        selected = markType == FolderMarkType.PHOTO,
                        onClick = {
                            markType = FolderMarkType.PHOTO
                            if (existing?.markType != FolderMarkType.PHOTO) {
                                previewUri = null
                                mimeType = null
                            }
                        },
                        label = { Text(stringResource(R.string.folder_mark_photo)) }
                    )
                    FilterChip(
                        selected = markType == FolderMarkType.VIDEO,
                        onClick = {
                            markType = FolderMarkType.VIDEO
                            if (existing?.markType != FolderMarkType.VIDEO) {
                                previewUri = null
                                mimeType = null
                            }
                        },
                        label = { Text(stringResource(R.string.folder_mark_video)) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                FolderMarkPreview(
                    markType = markType,
                    colorHex = colorHex,
                    previewUri = previewUri,
                    existing = existing,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                when (markType) {
                    FolderMarkType.COLOR -> {
                        BreezTextButton(
                            text = stringResource(R.string.folder_color),
                            onClick = { colorPicker = true }
                        )
                    }
                    FolderMarkType.PHOTO -> {
                        BreezTextButton(
                            text = stringResource(R.string.folder_pick_photo),
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                    FolderMarkType.VIDEO -> {
                        BreezTextButton(
                            text = stringResource(R.string.folder_pick_video),
                            onClick = {
                                videoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                        )
                        Text(
                            text = stringResource(R.string.folder_video_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                if (!shownError.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = shownError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

    if (colorPicker) {
        ColorPickerDialog(
            initialColor = parseHexColor(colorHex),
            onColorSelected = { colorHex = it.toHex() },
            onDismiss = { colorPicker = false }
        )
    }
}

private fun videoDurationMs(context: android.content.Context, uri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (_: Exception) {
        0L
    } finally {
        runCatching { retriever.release() }
    }
}
