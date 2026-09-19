package com.breez.notes.ui.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField

@Composable
fun MeetingPlaceSection(
    place: String,
    latitude: Double?,
    longitude: Double?,
    locationReminder: Boolean,
    onPlaceChange: (String) -> Unit,
    onResolve: () -> Unit,
    onUseCurrent: () -> Unit,
    onLocationReminderChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.meeting_title), style = MaterialTheme.typography.titleMedium)
        BreezTextField(
            value = place,
            onValueChange = onPlaceChange,
            hint = stringResource(R.string.meeting_hint),
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BreezTextButton(text = stringResource(R.string.meeting_find), onClick = onResolve)
            BreezTextButton(text = stringResource(R.string.meeting_current), onClick = onUseCurrent)
            BreezTextButton(
                text = stringResource(R.string.meeting_open_map),
                onClick = {
                    val uri = if (latitude != null && longitude != null) {
                        val label = Uri.encode(place.ifBlank { context.getString(R.string.meeting_title) })
                        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
                    } else {
                        Uri.parse("geo:0,0?q=${Uri.encode(place)}")
                    }
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                }
            )
        }
        if (latitude != null && longitude != null) {
            Text(
                text = stringResource(R.string.meeting_coords, latitude, longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.meeting_nearby),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(checked = locationReminder, onCheckedChange = onLocationReminderChange)
        }
    }
}
