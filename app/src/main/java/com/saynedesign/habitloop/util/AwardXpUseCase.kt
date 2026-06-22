package com.saynedesign.habitloop.util

import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.XpEventDao
import com.saynedesign.habitloop.data.XpEventEntity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AwardXpUseCase @Inject constructor(
    private val userDao: UserDao,
    private val xpEventDao: XpEventDao
) {
    suspend fun award(amount: Int, reason: String, habitId: Long? = null) {
        try {
            xpEventDao.insertEvent(
                XpEventEntity(
                    xpAmount = amount,
                    reason = reason,
                    relatedHabitId = habitId
                )
            )
            val user = userDao.getUserOneShot()
            val newXp = (user?.totalXp ?: 0) + amount
            val newLevel = LevelSystem.getLevelForXp(newXp)
            userDao.updateXp(newXp, newLevel)
        } catch (_: Exception) {
            // XP failures should never block core flows
        }
    }
}
