package com.saynedesign.habitloop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface XpEventDao {
    @Insert
    suspend fun insertEvent(event: XpEventEntity)

    @Query("SELECT * FROM xp_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentEvents(): Flow<List<XpEventEntity>>

    @Query("SELECT COALESCE(SUM(xpAmount), 0) FROM xp_events")
    fun getTotalXp(): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpAmount), 0) FROM xp_events WHERE relatedHabitId = :habitId")
    fun getXpForHabit(habitId: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpAmount), 0) FROM xp_events WHERE timestamp BETWEEN :start AND :end")
    fun getXpInRange(start: Long, end: Long): Flow<Int>
}
