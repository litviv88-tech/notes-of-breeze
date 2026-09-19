package com.breez.notes.ui.folders

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PlayCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.FolderMarkType
import com.breez.notes.ui.theme.parseHexColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun Folder.markFile(context: Context): File? {
    if (markFileName.isBlank()) return null
    val file = File(context.filesDir, "folder_marks/$id/$markFileName")
    return file.takeIf { it.exists() }
}

@Composable
fun FolderMarkBadge(
    folder: Folder,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    FolderMarkVisual(
        markType = folder.markType,
        colorHex = folder.colorHex,
        file = folder.markFile(LocalContext.current),
        previewUri = null,
        modifier = modifier.size(size),
        circle = true,
        showPlayIcon = folder.markType == FolderMarkType.VIDEO
    )
}

@Composable
fun FolderMarkCover(
    folder: Folder,
    modifier: Modifier = Modifier,
    playVideo: Boolean = false
) {
    val context = LocalContext.current
    val file = folder.markFile(context)
    FolderMarkVisual(
        markType = folder.markType,
        colorHex = folder.colorHex,
        file = file,
        previewUri = null,
        modifier = modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        circle = false,
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
    size: Dp = 72.dp
) {
    val context = LocalContext.current
    val existingFile = existing
        ?.takeIf { it.markType == markType && previewUri == null }
        ?.markFile(context)
    FolderMarkVisual(
        markType = markType,
        colorHex = colorHex,
        file = existingFile,
        previewUri = previewUri,
        modifier = modifier.size(size),
        circle = true,
        showPlayIcon = markType == FolderMarkType.VIDEO
    )
}

@Composable
private fun FolderMarkVisual(
    markType: FolderMarkType,
    colorHex: String,
    file: File?,
    previewUri: Uri?,
    modifier: Modifier,
    circle: Boolean,
    showPlayIcon: Boolean,
    playVideo: Boolean = false
) {
    val shape = if (circle) CircleShape else RoundedCornerShape(0.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (markType == FolderMarkType.COLOR) {
                    parseHexColor(colorHex)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            playVideo && file != null -> LoopingMutedVideo(file = file, modifier = Modifier.fillMaxSize())
            markType == FolderMarkType.PHOTO && previewUri != null -> {
                AsyncImage(
                    model = previewUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            markType == FolderMarkType.PHOTO && file != null -> {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            markType == FolderMarkType.VIDEO && (previewUri != null || file != null) -> {
                VideoStill(
                    source = previewUri ?: file!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
            markType == FolderMarkType.COLOR -> {
                if (!circle) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f)
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
            Icon(
                imageVector = Icons.Outlined.PlayCircle,
                contentDescription = stringResource(R.string.attachments_play),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LoopingMutedVideo(
    file: File,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setOnPreparedListener { player ->
                    player.isLooping = true
                    player.setVolume(0f, 0f)
                    start()
                }
                setOnErrorListener { _, _, _ -> true }
                setVideoPath(file.absolutePath)
            }
        },
        update = { view ->
            if (view.tag != file.absolutePath) {
                view.tag = file.absolutePath
                view.setVideoPath(file.absolutePath)
            }
        }
    )
}

@Composable
private fun VideoStill(
    source: Any,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(source) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(source) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                when (source) {
                    is File -> retriever.setDataSource(source.absolutePath)
                    is Uri -> retriever.setDataSource(context, source)
                    else -> return@runCatching null
                }
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
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Videocam, contentDescription = null)
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
