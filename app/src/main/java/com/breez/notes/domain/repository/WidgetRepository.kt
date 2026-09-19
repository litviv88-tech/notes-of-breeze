package com.breez.notes.domain.repository

import com.breez.notes.domain.model.WidgetConfig
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    fun observeAll(): Flow<List<WidgetConfig>>
    suspend fun getByAppWidgetId(appWidgetId: Int): WidgetConfig?
    suspend fun upsert(config: WidgetConfig): Long
    suspend fun deleteByAppWidgetId(appWidgetId: Int)
}
