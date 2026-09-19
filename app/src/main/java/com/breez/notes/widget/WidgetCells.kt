package com.breez.notes.widget

object WidgetCells {
    const val MIN_WIDTH = 1
    const val MAX_WIDTH = 6
    const val MIN_HEIGHT = 2
    const val MAX_HEIGHT = 5
    const val DEFAULT_WIDTH = 3
    const val DEFAULT_HEIGHT = 2

    fun coerceWidth(value: Int): Int = value.coerceIn(MIN_WIDTH, MAX_WIDTH)

    fun coerceHeight(value: Int): Int = value.coerceIn(MIN_HEIGHT, MAX_HEIGHT)

    fun fromDp(widthDp: Int, heightDp: Int): Pair<Int, Int> {
        return coerceWidth(cellsFromDp(widthDp)) to coerceHeight(cellsFromDp(heightDp))
    }

    private fun cellsFromDp(dp: Int): Int {
        if (dp <= 0) return 1
        return ((dp + 30) / 70).coerceAtLeast(1)
    }
}
