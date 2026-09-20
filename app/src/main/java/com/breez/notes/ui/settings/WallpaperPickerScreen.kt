package com.breez.notes.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.domain.model.WallpaperSettings
import com.breez.notes.domain.model.WallpaperType
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.components.ColorPickerDialog
import com.breez.notes.ui.components.WallpaperBackground
import com.breez.notes.ui.media.VideoTrimDialog
import com.breez.notes.ui.theme.BuiltInWallpapers
import com.breez.notes.ui.theme.Transparent
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.toHex

@Composable
fun WallpaperPickerScreen(
    onBack: () -> Unit,
    viewModel: WallpaperViewModel = hiltViewModel()
) {
    val settings by viewModel.preview.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var colorPicker by remember { mutableStateOf(false) }
    var applied by remember { mutableStateOf(false) }
    var trimUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        viewModel.update {
            it.copy(
                wallpaperType = WallpaperType.PHOTO,
                wallpaperUri = uri.toString()
            )
        }
        tab = 1
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { trimUri = it }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(settings = settings)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Transparent,
            topBar = { BreezTopBar(title = stringResource(R.string.wallpaper_title), onBack = onBack, transparent = true) }
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = Transparent,
                edgePadding = 0.dp
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.wallpaper_tab_builtin)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.wallpaper_tab_gallery)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.wallpaper_tab_live)) })
                Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text(stringResource(R.string.wallpaper_tab_color)) })
            }
            SpacerPreview()
            when (tab) {
                0 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(280.dp),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BuiltInWallpapers) { wallpaper ->
                            val selected = settings.builtInWallpaperId == wallpaper.id &&
                                settings.wallpaperType == WallpaperType.BUILTIN
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(wallpaper.brush)
                                    .then(
                                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                                        else Modifier
                                    )
                                    .clickable {
                                        viewModel.update {
                                            it.copy(
                                                wallpaperType = WallpaperType.BUILTIN,
                                                builtInWallpaperId = wallpaper.id
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
                1 -> {
                    BreezButton(
                        text = stringResource(R.string.wallpaper_pick_photo),
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                2 -> {
                    Text(
                        text = stringResource(R.string.wallpaper_live_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SpacerPreview()
                    BreezButton(
                        text = stringResource(R.string.wallpaper_pick_video),
                        onClick = {
                            videoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    BreezButton(
                        text = stringResource(R.string.wallpaper_pick_color),
                        onClick = { colorPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            SpacerPreview()
            Text(stringResource(R.string.wallpaper_dim))
            Slider(
                value = settings.wallpaperDim,
                onValueChange = { value -> viewModel.update { it.copy(wallpaperDim = value) } },
                valueRange = 0f..0.8f
            )
            Text(stringResource(R.string.wallpaper_blur))
            Slider(
                value = settings.wallpaperBlur,
                onValueChange = { value -> viewModel.update { it.copy(wallpaperBlur = value) } },
                valueRange = 0f..25f
            )
            BreezButton(
                text = if (applied) stringResource(R.string.wallpaper_applied) else stringResource(R.string.wallpaper_apply),
                onClick = {
                    viewModel.apply()
                    applied = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
    if (colorPicker) {
        ColorPickerDialog(
            initialColor = parseHexColor(settings.wallpaperColorHex ?: "#4A90E2"),
            onColorSelected = { color ->
                viewModel.update {
                    it.copy(
                        wallpaperType = WallpaperType.COLOR,
                        wallpaperColorHex = color.toHex()
                    )
                }
            },
            onDismiss = { colorPicker = false }
        )
    }
    val pendingTrim = trimUri
    if (pendingTrim != null) {
        VideoTrimDialog(
            source = pendingTrim,
            maxDurationMs = WallpaperSettings.MAX_VIDEO_DURATION_MS,
            hint = stringResource(R.string.video_trim_hint_wallpaper),
            stripAudio = true,
            onConfirm = { file ->
                viewModel.setLiveVideo(file)
                tab = 2
                trimUri = null
            },
            onDismiss = { trimUri = null }
        )
    }
}

@Composable
private fun SpacerPreview() {
    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
}
