package com.breez.notes.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.breez.notes.R
import com.breez.notes.domain.model.NoteAttachment
import com.breez.notes.ui.components.BreezTextButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AttachmentSection(
    attachments: List<NoteAttachment>,
    busy: Boolean,
    onAddPhoto: () -> Unit,
    onAddVideo: () -> Unit,
    onRemove: (NoteAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(R.string.attachments_title), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BreezTextButton(text = stringResource(R.string.attachments_add_photo), onClick = onAddPhoto)
            BreezTextButton(text = stringResource(R.string.attachments_add_video), onClick = onAddVideo)
        }
        if (busy) {
            Text(
                text = stringResource(R.string.attachments_busy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        if (attachments.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments, key = { it.id }) { item ->
                    val file = remember(item.id, item.fileName) {
                        File(context.filesDir, "attachments/${item.noteId}/${item.fileName}")
                    }
                    AttachmentThumb(
                        attachment = item,
                        file = file,
                        onOpen = { openAttachment(context, file, item.mimeType) },
                        onRemove = { onRemove(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentThumb(
    attachment: NoteAttachment,
    file: File,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onOpen)
    ) {
        if (attachment.isPhoto) {
            AsyncImage(
                model = file,
                contentDescription = attachment.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            VideoFrame(file = file)
            Icon(
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = stringResource(R.string.attachments_play),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.attachments_remove))
        }
    }
}

@Composable
private fun VideoFrame(file: File) {
    var bitmap by remember(file.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(file.path) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val frame: Bitmap? = retriever.getFrameAtTime(0)
                retriever.release()
                frame?.asImageBitmap()
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Videocam, contentDescription = null)
        }
    }
}

private fun openAttachment(context: android.content.Context, file: File, mimeType: String) {
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, mimeType.ifBlank { "*/*" })
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    runCatching { context.startActivity(intent) }
}
