package com.codesmithslabs.thedogtail.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs ORDER BY dateEpochDay DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay BETWEEN :start AND :end")
    fun getLogsBetween(start: Long, end: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLogForDay(habitId: Long, dateEpochDay: Long): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateEpochDay DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Delete
    suspend fun deleteLog(log: HabitLogEntity)

    @Query("SELECT COUNT(*) FROM habit_logs")
    fun countAllLogs(): Flow<Int>

    @Query("SELECT * FROM habit_logs WHERE dateEpochDay = :dateEpochDay")
    suspend fun getLogsForDayOneShot(dateEpochDay: Long): List<HabitLogEntity>

    @Query("SELECT COUNT(DISTINCT dateEpochDay) FROM habit_logs WHERE habitId = :habitId AND dateEpochDay BETWEEN :startDay AND :endDay")
    suspend fun countLogDaysInRange(habitId: Long, startDay: Long, endDay: Long): Int
}

