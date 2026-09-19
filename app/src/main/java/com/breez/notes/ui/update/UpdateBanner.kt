package com.breez.notes.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.AppUpdateState
import com.breez.notes.domain.model.UpdateStatus
import com.breez.notes.ui.components.BreezButton

@Composable
fun UpdateBanner(
    state: AppUpdateState,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.update_available_title,
                            state.latestVersion.ifBlank { state.currentVersion }
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.update_current,
                            state.currentVersion.ifBlank { "—" }
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = state.notes.ifBlank { stringResource(R.string.update_available_text) },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_close)
                    )
                }
            }
            if (state.status == UpdateStatus.Downloading) {
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.update_downloading, state.progress),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                BreezButton(
                    text = if (state.status == UpdateStatus.Error) {
                        stringResource(R.string.update_retry)
                    } else {
                        stringResource(R.string.update_now)
                    },
                    onClick = if (state.status == UpdateStatus.Error) onRetry else onUpdate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
