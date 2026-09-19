package com.breez.notes.domain.model

enum class RepeatUnit {
    NONE,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

data class Recurrence(
    val unit: RepeatUnit = RepeatUnit.NONE,
    val interval: Int = 1,
    val weekDays: Set<Int> = emptySet(),
    val untilAt: Long? = null
) {
    val isEnabled: Boolean get() = unit != RepeatUnit.NONE && interval >= 1

    fun encodeWeekDays(): String = weekDays.sorted().joinToString(",")

    companion object {
        fun from(
            unit: String?,
            interval: Int,
            weekDays: String?,
            untilAt: Long?
        ): Recurrence = Recurrence(
            unit = runCatching { RepeatUnit.valueOf(unit ?: RepeatUnit.NONE.name) }
                .getOrDefault(RepeatUnit.NONE),
            interval = interval.coerceIn(1, 99),
            weekDays = weekDays.orEmpty()
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet(),
            untilAt = untilAt
        )
    }
}
