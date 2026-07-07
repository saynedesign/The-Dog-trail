package com.saynedesign.habitloop.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * Ledger of coach notifications that were actually shown. Used to enforce
 * per-rule cooldowns (so the coach never nags) and, over time, to analyze
 * which nudges land.
 */
@Entity(tableName = "coach_events")
data class CoachEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Rule type, e.g. STREAK_DANGER, BREAK_CHECKIN, EVENING_RESCUE, BURNOUT, CELEBRATION, WEEKLY_DIGEST */
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CoachEventDao {
    @Insert
    suspend fun insert(event: CoachEventEntity)

    @Query("SELECT MAX(timestamp) FROM coach_events WHERE type = :type")
    suspend fun lastEventOfType(type: String): Long?

    @Query("SELECT COUNT(*) FROM coach_events WHERE type = :type AND timestamp >= :since")
    suspend fun countOfTypeSince(type: String, since: Long): Int
}
