package com.breez.notes.reminders

import com.breez.notes.domain.model.Recurrence
import com.breez.notes.domain.model.RepeatUnit
import java.util.Calendar

object RecurrenceCalculator {

    fun nextAfter(fromMillis: Long, recurrence: Recurrence): Long {
        if (!recurrence.isEnabled) return fromMillis
        val calendar = Calendar.getInstance().apply { timeInMillis = fromMillis }
        when (recurrence.unit) {
            RepeatUnit.NONE -> Unit
            RepeatUnit.DAY -> calendar.add(Calendar.DAY_OF_MONTH, recurrence.interval)
            RepeatUnit.MONTH -> calendar.add(Calendar.MONTH, recurrence.interval)
            RepeatUnit.YEAR -> calendar.add(Calendar.YEAR, recurrence.interval)
            RepeatUnit.WEEK -> advanceWeekly(calendar, recurrence)
        }
        return calendar.timeInMillis
    }

    fun nextUpcoming(fromMillis: Long, recurrence: Recurrence, now: Long = System.currentTimeMillis()): Long? {
        if (!recurrence.isEnabled) return null
        var next = if (fromMillis > now) fromMillis else nextAfter(fromMillis, recurrence)
        var guard = 0
        while (next <= now && guard++ < 1000) {
            next = nextAfter(next, recurrence)
        }
        val until = recurrence.untilAt
        if (until != null && next > until) return null
        return next
    }

    private fun advanceWeekly(calendar: Calendar, recurrence: Recurrence) {
        val selected = recurrence.weekDays.ifEmpty { setOf(calendar.get(Calendar.DAY_OF_WEEK)) }
        val originWeek = calendar.clone() as Calendar
        originWeek.set(Calendar.HOUR_OF_DAY, 0)
        originWeek.set(Calendar.MINUTE, 0)
        originWeek.set(Calendar.SECOND, 0)
        originWeek.set(Calendar.MILLISECOND, 0)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        repeat(14 * recurrence.interval.coerceAtLeast(1)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            if (day in selected) {
                val weeks = weeksBetween(originWeek, calendar)
                if (weeks % recurrence.interval == 0) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    return
                }
            }
        }
        calendar.add(Calendar.WEEK_OF_YEAR, recurrence.interval)
    }

    private fun weeksBetween(from: Calendar, to: Calendar): Int {
        val fromDay = from.clone() as Calendar
        fromDay.firstDayOfWeek = Calendar.MONDAY
        fromDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val toDay = to.clone() as Calendar
        toDay.firstDayOfWeek = Calendar.MONDAY
        toDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val ms = toDay.timeInMillis - fromDay.timeInMillis
        return (ms / (7L * 24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    }
}
