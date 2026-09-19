package com.breez.notes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.breez.notes.data.local.entity.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs")
    fun observeAll(): Flow<List<WidgetConfigEntity>>

    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getByAppWidgetId(appWidgetId: Int): WidgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: WidgetConfigEntity): Long

    @Update
    suspend fun update(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun deleteByAppWidgetId(appWidgetId: Int)
}
