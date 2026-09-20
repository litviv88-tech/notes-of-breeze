package com.breez.notes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.AppUpdateState
import com.breez.notes.domain.model.UpdateStatus
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.theme.Transparent

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenWeb: () -> Unit = {},
    updateState: AppUpdateState = AppUpdateState(),
    onCheckUpdate: () -> Unit = {},
    onStartUpdate: () -> Unit = {}
) {
    var aboutOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = { BreezTopBar(title = stringResource(R.string.settings_title), onBack = onBack, transparent = true) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_appearance)) },
                supportingContent = { Text(stringResource(R.string.settings_appearance_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onOpenTheme)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
                supportingContent = { Text(stringResource(R.string.settings_wallpaper_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Image, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onOpenWallpaper)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_web)) },
                supportingContent = { Text(stringResource(R.string.settings_web_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onOpenWeb)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about)) },
                supportingContent = { Text(stringResource(R.string.settings_about_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable { aboutOpen = true }
            )
            Spacer(Modifier.weight(1f))
            UpdateSettingsBlock(
                state = updateState,
                onCheckUpdate = onCheckUpdate,
                onStartUpdate = onStartUpdate,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    if (aboutOpen) {
        AlertDialog(
            onDismissRequest = { aboutOpen = false },
            confirmButton = {
                BreezButton(text = stringResource(R.string.action_close), onClick = { aboutOpen = false })
            },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.about_version))
                    Text(stringResource(R.string.about_description))
                    Text(stringResource(R.string.about_package))
                }
            }
        )
    }
}

@Composable
fun UpdateSettingsBlock(
    state: AppUpdateState,
    onCheckUpdate: () -> Unit,
    onStartUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (state.status) {
        UpdateStatus.Checking -> stringResource(R.string.update_checking)
        UpdateStatus.UpToDate -> stringResource(R.string.update_latest)
        UpdateStatus.Available, UpdateStatus.Downloading -> stringResource(
            R.string.update_available_title,
            state.latestVersion.ifBlank { "—" }
        )
        UpdateStatus.Error -> stringResource(R.string.update_failed)
        UpdateStatus.Idle -> null
    }
    val releasedVersion = state.latestVersion.ifBlank { state.currentVersion }.ifBlank { "—" }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.update_title)) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.update_released, releasedVersion))
                    if (statusText != null) {
                        Text(statusText)
                    }
                }
            },
            leadingContent = { Icon(Icons.Outlined.SystemUpdate, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Transparent)
        )
        if (state.status == UpdateStatus.Downloading) {
            LinearProgressIndicator(
                progress = { state.progress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.hasUpdate) {
            BreezButton(
                text = if (state.status == UpdateStatus.Error) {
                    stringResource(R.string.update_retry)
                } else {
                    stringResource(R.string.update_now)
                },
                onClick = onStartUpdate,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.status != UpdateStatus.Downloading
            )
        } else {
            BreezButton(
                text = stringResource(R.string.update_check),
                onClick = onCheckUpdate,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.status != UpdateStatus.Checking
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
