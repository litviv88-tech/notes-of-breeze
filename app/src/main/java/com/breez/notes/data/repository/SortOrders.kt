package com.breez.notes.data.repository

internal fun sortOrdersAfterReorder(currentOrders: List<Int>): List<Int> {
    if (currentOrders.isEmpty()) return emptyList()
    return if (currentOrders.distinct().size == currentOrders.size) {
        currentOrders.sorted()
    } else {
        val start = currentOrders.min()
        currentOrders.indices.map { start + it }
    }
}
