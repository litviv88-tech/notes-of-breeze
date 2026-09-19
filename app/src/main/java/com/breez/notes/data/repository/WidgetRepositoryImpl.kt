package com.breez.notes.data.repository

import com.breez.notes.data.local.dao.WidgetConfigDao
import com.breez.notes.data.mapper.toDomain
import com.breez.notes.data.mapper.toEntity
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.repository.WidgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepositoryImpl @Inject constructor(
    private val widgetConfigDao: WidgetConfigDao
) : WidgetRepository {

    override fun observeAll(): Flow<List<WidgetConfig>> =
        widgetConfigDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getByAppWidgetId(appWidgetId: Int): WidgetConfig? =
        widgetConfigDao.getByAppWidgetId(appWidgetId)?.toDomain()

    override suspend fun upsert(config: WidgetConfig): Long {
        val existing = widgetConfigDao.getByAppWidgetId(config.appWidgetId)
        val entity = config.toEntity()
        return if (existing == null) {
            widgetConfigDao.insert(entity.copy(id = 0L))
        } else {
            widgetConfigDao.update(entity.copy(id = existing.id))
            existing.id
        }
    }

    override suspend fun deleteByAppWidgetId(appWidgetId: Int) {
        widgetConfigDao.deleteByAppWidgetId(appWidgetId)
    }
}
