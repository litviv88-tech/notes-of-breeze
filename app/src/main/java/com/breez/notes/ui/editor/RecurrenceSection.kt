package com.breez.notes.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.Recurrence
import com.breez.notes.domain.model.RepeatUnit
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField
import java.util.Calendar

private data class WeekDayChip(val calendarDay: Int, val labelRes: Int)

private val weekDays = listOf(
    WeekDayChip(Calendar.MONDAY, R.string.repeat_mon),
    WeekDayChip(Calendar.TUESDAY, R.string.repeat_tue),
    WeekDayChip(Calendar.WEDNESDAY, R.string.repeat_wed),
    WeekDayChip(Calendar.THURSDAY, R.string.repeat_thu),
    WeekDayChip(Calendar.FRIDAY, R.string.repeat_fri),
    WeekDayChip(Calendar.SATURDAY, R.string.repeat_sat),
    WeekDayChip(Calendar.SUNDAY, R.string.repeat_sun)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSection(
    recurrence: Recurrence,
    onChange: (Recurrence) -> Unit,
    modifier: Modifier = Modifier
) {
    var untilPicker by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.repeat_title), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitChip(RepeatUnit.NONE, R.string.repeat_none, recurrence, onChange)
            UnitChip(RepeatUnit.DAY, R.string.repeat_day, recurrence, onChange)
            UnitChip(RepeatUnit.WEEK, R.string.repeat_week, recurrence, onChange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitChip(RepeatUnit.MONTH, R.string.repeat_month, recurrence, onChange)
            UnitChip(RepeatUnit.YEAR, R.string.repeat_year, recurrence, onChange)
        }
        if (recurrence.isEnabled) {
            BreezTextField(
                value = recurrence.interval.toString(),
                onValueChange = { raw ->
                    val value = raw.filter(Char::isDigit).toIntOrNull()?.coerceIn(1, 99) ?: 1
                    onChange(recurrence.copy(interval = value))
                },
                hint = stringResource(R.string.repeat_interval),
                singleLine = true
            )
            Text(
                text = stringResource(R.string.repeat_interval_hint, recurrence.interval, unitLabel(recurrence.unit)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        if (recurrence.unit == RepeatUnit.WEEK) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekDays.forEach { day ->
                    val selected = day.calendarDay in recurrence.weekDays
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val next = recurrence.weekDays.toMutableSet()
                            if (selected) next.remove(day.calendarDay) else next.add(day.calendarDay)
                            onChange(recurrence.copy(weekDays = next))
                        },
                        label = { Text(stringResource(day.labelRes)) }
                    )
                }
            }
        }
        if (recurrence.isEnabled) {
            BreezTextButton(
                text = if (recurrence.untilAt == null) {
                    stringResource(R.string.repeat_until_none)
                } else {
                    stringResource(R.string.repeat_until_set)
                },
                onClick = { untilPicker = true }
            )
            if (recurrence.untilAt != null) {
                BreezTextButton(
                    text = stringResource(R.string.repeat_until_clear),
                    onClick = { onChange(recurrence.copy(untilAt = null)) }
                )
            }
        }
    }
    if (untilPicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = recurrence.untilAt)
        DatePickerDialog(
            onDismissRequest = { untilPicker = false },
            confirmButton = {
                BreezTextButton(
                    text = stringResource(R.string.editor_confirm),
                    onClick = {
                        onChange(recurrence.copy(untilAt = dateState.selectedDateMillis))
                        untilPicker = false
                    }
                )
            },
            dismissButton = {
                BreezTextButton(
                    text = stringResource(R.string.editor_cancel),
                    onClick = { untilPicker = false }
                )
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun UnitChip(
    unit: RepeatUnit,
    labelRes: Int,
    recurrence: Recurrence,
    onChange: (Recurrence) -> Unit
) {
    FilterChip(
        selected = recurrence.unit == unit,
        onClick = { onChange(recurrence.copy(unit = unit)) },
        label = { Text(stringResource(labelRes)) }
    )
}

@Composable
private fun unitLabel(unit: RepeatUnit): String = stringResource(
    when (unit) {
        RepeatUnit.NONE -> R.string.repeat_none
        RepeatUnit.DAY -> R.string.repeat_day_unit
        RepeatUnit.WEEK -> R.string.repeat_week_unit
        RepeatUnit.MONTH -> R.string.repeat_month_unit
        RepeatUnit.YEAR -> R.string.repeat_year_unit
    }
)
