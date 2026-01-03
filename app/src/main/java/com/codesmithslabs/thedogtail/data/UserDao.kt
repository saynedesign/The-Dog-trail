package com.codesmithslabs.thedogtail.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_info LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_info LIMIT 1")
    suspend fun getUserOneShot(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE user_info SET journal = :journal, journalLastUpdated = :updatedAt WHERE id = :id")
    suspend fun updateJournal(id: Long, journal: String, updatedAt: Long)
}
