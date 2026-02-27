package com.codesmithslabs.thedogtail.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Query("SELECT * FROM moods WHERE timestamp BETWEEN :start AND :end")
    fun getMoodsForRange(start: Long, end: Long): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE timestamp = :date LIMIT 1")
    suspend fun getMoodForDate(date: Long): MoodEntity?

    @Query("SELECT * FROM moods ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>
}
