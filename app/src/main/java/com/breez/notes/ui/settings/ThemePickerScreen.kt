package com.breez.notes.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.domain.model.Palette
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezCard
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.components.ColorPickerDialog
import com.breez.notes.ui.theme.Transparent
import com.breez.notes.ui.theme.parseHexColor
import com.breez.notes.ui.theme.toHex

@Composable
fun ThemePickerScreen(
    onBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var colorPicker by remember { mutableStateOf(false) }
    val modes = listOf(
        ThemeMode.SYSTEM to R.string.theme_mode_system,
        ThemeMode.LIGHT to R.string.theme_mode_light,
        ThemeMode.DARK to R.string.theme_mode_dark,
        ThemeMode.AUTO to R.string.theme_mode_auto
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = { BreezTopBar(title = stringResource(R.string.theme_title), onBack = onBack, transparent = true) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.theme_preview), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            BreezCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.theme_preview_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.theme_preview_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.theme_mode), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            modes.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { (mode, labelRes) ->
                        val selected = settings.themeMode == mode
                        OutlinedCard(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 8.dp)
                                .clickable { viewModel.setThemeMode(mode) },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.theme_palettes), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(220.dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(viewModel.palettes) { palette ->
                    val selected = settings.paletteId == palette.id && !settings.useMaterialYou
                    PaletteDot(
                        hex = palette.primaryHex,
                        selected = selected,
                        onClick = { viewModel.setPalette(palette.id) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.theme_material_you), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(R.string.theme_material_you_subtitle),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = settings.useMaterialYou,
                        onCheckedChange = viewModel::setMaterialYou
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            BreezButton(
                text = stringResource(R.string.theme_custom_color),
                onClick = { colorPicker = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (colorPicker) {
        ColorPickerDialog(
            initialColor = parseHexColor(settings.customColorHex ?: Palette.CUSTOM_ID.let { "#4A90E2" }),
            onColorSelected = { viewModel.setCustomColor(it.toHex()) },
            onDismiss = { colorPicker = false }
        )
    }
}

@Composable
private fun PaletteDot(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = parseHexColor(hex)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}
