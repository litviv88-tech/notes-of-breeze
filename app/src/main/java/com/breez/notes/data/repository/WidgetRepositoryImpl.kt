package com.breez.notes.data.repository

import android.content.Context
import com.breez.notes.data.local.dao.WidgetConfigDao
import com.breez.notes.data.mapper.toDomain
import com.breez.notes.data.mapper.toEntity
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.repository.WidgetRepository
import com.breez.notes.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    private val widgetConfigDao: WidgetConfigDao,
    @ApplicationContext private val context: Context
) : WidgetRepository {

    override fun observeAll(): Flow<List<WidgetConfig>> =
        widgetConfigDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByAppWidgetId(appWidgetId: Int): WidgetConfig? =
        widgetConfigDao.getByAppWidgetId(appWidgetId)?.toDomain()

    override suspend fun upsert(config: WidgetConfig): Long {
        val existing = widgetConfigDao.getByAppWidgetId(config.appWidgetId)
        val entity = config.toEntity()
        val id = if (existing == null) {
            widgetConfigDao.insert(entity.copy(id = 0L))
        } else {
            widgetConfigDao.update(entity.copy(id = existing.id))
            existing.id
        }
        WidgetUpdater.updateAll(context)
        return id
    }

    override suspend fun deleteByAppWidgetId(appWidgetId: Int) {
        widgetConfigDao.deleteByAppWidgetId(appWidgetId)
        WidgetUpdater.updateAll(context)
    }
}
