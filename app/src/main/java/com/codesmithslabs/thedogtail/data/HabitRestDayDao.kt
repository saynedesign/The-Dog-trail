package com.codesmithslabs.thedogtail.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitRestDayDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun declareRestDay(restDay: HabitRestDayEntity)

    @Query("SELECT COUNT(*) FROM habit_rest_days WHERE habitId = :habitId AND dateEpochDay BETWEEN :weekStart AND :weekEnd")
    suspend fun countRestDaysInWeek(habitId: Long, weekStart: Long, weekEnd: Long): Int

    @Query("SELECT dateEpochDay FROM habit_rest_days WHERE habitId = :habitId")
    fun getRestDayEpochsForHabit(habitId: Long): Flow<List<Long>>

    @Query("SELECT * FROM habit_rest_days WHERE dateEpochDay = :epoch")
    suspend fun getRestDaysForDay(epoch: Long): List<HabitRestDayEntity>

    @Query("SELECT habitId FROM habit_rest_days WHERE dateEpochDay = :epoch")
    suspend fun getRestingHabitIdsForDay(epoch: Long): List<Long>

    @Query("SELECT dateEpochDay FROM habit_rest_days")
    fun getAllRestDayEpochs(): Flow<List<Long>>

    @Query("SELECT * FROM habit_rest_days")
    fun getAllRestDays(): Flow<List<HabitRestDayEntity>>
}
