package com.breez.notes.ui.folders

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.breez.notes.R
import com.breez.notes.data.files.FolderMarkStore
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.ui.media.LoopingTextureVideo
import com.breez.notes.ui.theme.PureWhite
import com.breez.notes.ui.theme.parseHexColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun Folder.markFile(context: Context): File? {
    if (markFileName.isBlank()) return null
    val file = File(context.filesDir, "folder_marks/$id/$markFileName")
    return file.takeIf { it.exists() }
}

fun Folder.markThumb(context: Context): File? {
    val file = File(context.filesDir, "folder_marks/$id/${FolderMarkStore.THUMB_NAME}")
    return file.takeIf { it.exists() }
}

@Composable
fun FolderMarkBadge(
    folder: Folder,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val ring = size >= 22.dp
    FolderMarkVisual(
        markType = folder.markType,
        colorHex = folder.colorHex,
        file = folder.markFile(LocalContext.current),
        thumb = folder.markThumb(LocalContext.current),
        previewUri = null,
        modifier = modifier
            .size(size)
            .then(
                if (ring) {
                    Modifier
                        .shadow(2.dp, CircleShape)
                        .border(1.dp, PureWhite.copy(alpha = 0.55f), CircleShape)
                } else {
                    Modifier
                }
            ),
        shape = CircleShape,
        showPlayIcon = folder.markType == FolderMarkType.VIDEO && size >= 20.dp
    )
}

@Composable
fun FolderMarkCover(
    folder: Folder,
    modifier: Modifier = Modifier,
    playVideo: Boolean = false,
    shape: Shape = RectangleShape
) {
    val context = LocalContext.current
    FolderMarkVisual(
        markType = folder.markType,
        colorHex = folder.colorHex,
        file = folder.markFile(context),
        thumb = folder.markThumb(context),
        previewUri = null,
        modifier = modifier,
        shape = shape,
        showPlayIcon = folder.markType == FolderMarkType.VIDEO && !playVideo,
        playVideo = playVideo && folder.markType == FolderMarkType.VIDEO
    )
}

@Composable
fun FolderMarkPreview(
    markType: FolderMarkType,
    colorHex: String,
    previewUri: Uri?,
    existing: Folder?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val context = LocalContext.current
    val existingFile = existing
        ?.takeIf { it.markType == markType && previewUri == null }
        ?.markFile(context)
    val existingThumb = existing
        ?.takeIf { it.markType == markType && previewUri == null }
        ?.markThumb(context)
    FolderMarkVisual(
        markType = markType,
        colorHex = colorHex,
        file = existingFile,
        thumb = existingThumb,
        previewUri = previewUri,
        modifier = modifier
            .size(size)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        showPlayIcon = false,
        playVideo = markType == FolderMarkType.VIDEO && (previewUri != null || existingFile != null)
    )
}

@Composable
private fun FolderMarkVisual(
    markType: FolderMarkType,
    colorHex: String,
    file: File?,
    thumb: File?,
    previewUri: Uri?,
    modifier: Modifier,
    shape: Shape,
    showPlayIcon: Boolean,
    playVideo: Boolean = false
) {
    val color = parseHexColor(colorHex)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (markType == FolderMarkType.COLOR) {
                    Brush.linearGradient(listOf(color, color.darken()))
                } else {
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            playVideo && file != null -> {
                LoopingTextureVideo(file = file, modifier = Modifier.fillMaxSize())
            }
            playVideo && previewUri != null -> {
                LoopingTextureVideo(uri = previewUri, modifier = Modifier.fillMaxSize())
            }
            markType == FolderMarkType.PHOTO && previewUri != null -> {
                AsyncImage(
                    model = previewUri,
                    contentDescription = stringResource(R.string.folder_mark_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            markType == FolderMarkType.PHOTO && file != null -> {
                AsyncImage(
                    model = file,
                    contentDescription = stringResource(R.string.folder_mark_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            markType == FolderMarkType.VIDEO && (previewUri != null || file != null || thumb != null) -> {
                VideoStill(
                    source = previewUri ?: file ?: thumb!!,
                    thumb = thumb,
                    modifier = Modifier.fillMaxSize()
                )
            }
            markType == FolderMarkType.COLOR -> {
                if (shape != CircleShape) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = PureWhite.copy(alpha = 0.92f)
                    )
                }
            }
            else -> Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showPlayIcon) {
            Box(
                modifier = Modifier
                    .size(if (shape == CircleShape) 18.dp else 36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.attachments_play),
                    modifier = Modifier.size(if (shape == CircleShape) 12.dp else 22.dp),
                    tint = PureWhite
                )
            }
        }
    }
}

@Composable
private fun VideoStill(
    source: Any,
    thumb: File?,
    modifier: Modifier = Modifier
) {
    if (thumb != null) {
        AsyncImage(
            model = thumb,
            contentDescription = stringResource(R.string.folder_mark_video),
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }
    val context = LocalContext.current
    var bitmap by remember(source) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(source) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                when (source) {
                    is File -> retriever.setDataSource(source.absolutePath)
                    is Uri -> if (source.scheme == "file") {
                        retriever.setDataSource(source.path)
                    } else {
                        retriever.setDataSource(context, source)
                    }
                    else -> return@runCatching null
                }
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val timeUs = (duration.coerceAtLeast(400L) / 8L).coerceIn(200L, 1_200L) * 1_000L
                val frame: Bitmap? = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
                retriever.release()
                frame?.asImageBitmap()
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = stringResource(R.string.folder_mark_video),
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Videocam, contentDescription = null, tint = PureWhite)
        }
    }
}

fun openFolderMark(context: Context, folder: Folder) {
    val file = folder.markFile(context) ?: return
    val mime = when (folder.markType) {
        FolderMarkType.PHOTO -> "image/*"
        FolderMarkType.VIDEO -> "video/*"
        FolderMarkType.COLOR -> return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    runCatching { context.startActivity(intent) }
}

private fun Color.darken(factor: Float = 0.58f): Color = copy(
    red = red * factor,
    green = green * factor,
    blue = blue * factor
)
