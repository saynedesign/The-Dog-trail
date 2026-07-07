package com.saynedesign.habitloop.util

import com.saynedesign.habitloop.data.CoachEventDao
import com.saynedesign.habitloop.data.CoachEventEntity
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.data.isScheduledOn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Coach Engine: one decision layer with three voices — warning, check-in,
 * motivation. Evaluated once per evening; produces AT MOST ONE candidate so
 * the coach never spams. Cooldowns are enforced from the coach_events ledger.
 *
 * Personalization inputs finally put the onboarding profile to work:
 *  - motivationStyle → celebration framing
 *  - experienceLevel → warning intensity (beginners get a gentler coach)
 *  - vacation mode → coach is fully silent
 */
@Singleton
class CoachEngine @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val userDao: UserDao,
    private val coachEventDao: CoachEventDao,
    private val preferencesRepository: UserPreferencesRepository
) {

    data class Candidate(
        val type: String,
        val category: Category,
        val title: String,
        val message: String,
        /** Attach the "Mark today as rest 🌿" inline action */
        val includeRestAction: Boolean = false
    )

    enum class Category(val channelId: String, val channelName: String) {
        WARNING("coach_warnings", "Coach — Warnings"),
        CHECK_IN("coach_checkins", "Coach — Check-ins"),
        MOTIVATION("coach_motivation", "Coach — Motivation"),
        DIGEST("coach_digest", "Coach — Weekly Digest")
    }

    object Types {
        const val STREAK_DANGER = "STREAK_DANGER"
        const val BREAK_CHECKIN = "BREAK_CHECKIN"
        const val EVENING_RESCUE = "EVENING_RESCUE"
        const val BURNOUT = "BURNOUT"
        const val CELEBRATION = "CELEBRATION"
        const val WEEKLY_DIGEST = "WEEKLY_DIGEST"
    }

    /** Evaluate all rules and return the single highest-priority candidate, or null. */
    suspend fun evaluate(today: LocalDate = LocalDate.now()): Candidate? {
        if (preferencesRepository.isVacationMode.firstOrNull() == true) return null

        val user = userDao.getUserOneShot()
        val habits = habitDao.getAllHabitsOneShot()
        if (habits.isEmpty()) return null

        val todayEpoch = today.toEpochDay()
        val logs = habitLogDao.getLogsBetween(todayEpoch - 90, todayEpoch).first()
        val restDays = habitRestDayDao.getAllRestDays().first()

        val logsByDay = logs.groupBy { it.dateEpochDay }
        val restByDay = restDays.groupBy { it.dateEpochDay }
        val logsToday = logsByDay[todayEpoch].orEmpty()
        val loggedTodayIds = logsToday.map { it.habitId }.toSet()
        val restingTodayIds = restByDay[todayEpoch].orEmpty().map { it.habitId }.toSet()

        val scheduledToday = habits.filter { it.isScheduledOn(today) }
        val pendingToday = scheduledToday.filter { it.id !in loggedTodayIds && it.id !in restingTodayIds }
        val streak = computeStreak(logsByDay.keys, restByDay.keys, todayEpoch)

        val experience = user?.experienceLevel ?: ExperienceLevel.BEGINNER
        val style = user?.motivationStyle ?: MotivationStyle.SEEING_PROGRESS

        // Priority order: warnings > check-ins > motivation > digest. First pass wins.
        streakDanger(streak, logsToday.isEmpty(), pendingToday, experience)?.let { return it }
        breakCheckIn(logsByDay, restByDay, todayEpoch, logsToday.isEmpty())?.let { return it }
        eveningRescue(loggedTodayIds, pendingToday)?.let { return it }
        burnout(logsByDay, restByDay, todayEpoch, user?.createdTimestamp, experience)?.let { return it }
        celebration(scheduledToday, pendingToday, streak, style, user?.totalXp ?: 0)?.let { return it }
        weeklyDigest(today, logsByDay, todayEpoch, habits, streak)?.let { return it }
        return null
    }

    /** Record that a candidate was actually shown (starts its cooldown). */
    suspend fun recordShown(type: String) {
        coachEventDao.insert(CoachEventEntity(type = type))
    }

    // ---------------------------------------------------------------- rules

    private suspend fun streakDanger(
        streak: Int,
        nothingLoggedToday: Boolean,
        pendingToday: List<HabitEntity>,
        experience: ExperienceLevel
    ): Candidate? {
        if (experience == ExperienceLevel.BEGINNER) return null // gentler coach for beginners
        if (streak < 7 || !nothingLoggedToday || pendingToday.isEmpty()) return null
        if (!cooldownOver(Types.STREAK_DANGER, days = 2)) return null
        return Candidate(
            type = Types.STREAK_DANGER,
            category = Category.WARNING,
            title = "Your $streak-day streak is on the line",
            message = "Nothing logged yet today. One habit keeps it alive — ${pendingToday.first().title} takes the least effort to start."
        )
    }

    private suspend fun breakCheckIn(
        logsByDay: Map<Long, *>,
        restByDay: Map<Long, *>,
        todayEpoch: Long,
        nothingLoggedToday: Boolean
    ): Candidate? {
        if (!nothingLoggedToday) return null // they're back — don't nag
        val silentDays = (1L..2L).all { offset ->
            val day = todayEpoch - offset
            !logsByDay.containsKey(day) && !restByDay.containsKey(day)
        }
        if (!silentDays) return null
        if (!cooldownOver(Types.BREAK_CHECKIN, days = 3)) return null
        return Candidate(
            type = Types.BREAK_CHECKIN,
            category = Category.CHECK_IN,
            title = "Taking a break?",
            message = "No guilt — breaks are part of the loop. Log today as a rest day and your streak survives.",
            includeRestAction = true
        )
    }

    private suspend fun eveningRescue(
        loggedTodayIds: Set<Long>,
        pendingToday: List<HabitEntity>
    ): Candidate? {
        if (loggedTodayIds.isEmpty() || pendingToday.isEmpty()) return null
        if (!cooldownOver(Types.EVENING_RESCUE, days = 1)) return null
        val names = pendingToday.take(2).joinToString(", ") { it.title }
        val extra = if (pendingToday.size > 2) " +${pendingToday.size - 2} more" else ""
        return Candidate(
            type = Types.EVENING_RESCUE,
            category = Category.CHECK_IN,
            title = if (pendingToday.size == 1) "1 habit left today" else "${pendingToday.size} habits left today",
            message = "Still time for: $names$extra. Finish strong for the Perfect Day bonus (+${LevelSystem.XpRewards.PERFECT_DAY} XP)."
        )
    }

    private suspend fun burnout(
        logsByDay: Map<Long, *>,
        restByDay: Map<Long, *>,
        todayEpoch: Long,
        accountCreated: Long?,
        experience: ExperienceLevel
    ): Candidate? {
        // Needs enough history to be meaningful
        val accountAgeDays = accountCreated?.let {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - it)
        } ?: 0
        if (accountAgeDays < 14) return null

        fun activeRatio(daysBack: LongRange): Float {
            val active = daysBack.count { offset ->
                val day = todayEpoch - offset
                logsByDay.containsKey(day) || restByDay.containsKey(day)
            }
            return active.toFloat() / (daysBack.last - daysBack.first + 1)
        }

        val c30 = activeRatio(1L..30L)
        val c7 = activeRatio(1L..7L)
        if (c30 < 0.5f || c7 > c30 * 0.7f) return null // no meaningful drop
        if (!cooldownOver(Types.BURNOUT, days = 7)) return null

        val message = if (experience == ExperienceLevel.BEGINNER) {
            "Your pace has dipped this week — completely normal. A planned rest day beats a guilty gap."
        } else {
            "Consistency dropped from ${(c30 * 100).toInt()}% (30d) to ${(c7 * 100).toInt()}% (7d). Consider scheduling rest days before the dip becomes a stop."
        }
        return Candidate(
            type = Types.BURNOUT,
            category = Category.WARNING,
            title = "Pace check 🧭",
            message = message,
            includeRestAction = true
        )
    }

    private suspend fun celebration(
        scheduledToday: List<HabitEntity>,
        pendingToday: List<HabitEntity>,
        streak: Int,
        style: MotivationStyle,
        totalXp: Int
    ): Candidate? {
        if (scheduledToday.isEmpty() || pendingToday.isNotEmpty()) return null
        if (!cooldownOver(Types.CELEBRATION, days = 3)) return null
        val weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        if (coachEventDao.countOfTypeSince(Types.CELEBRATION, weekAgo) >= 2) return null

        val done = scheduledToday.size
        val (title, message) = when (style) {
            MotivationStyle.KEEPING_STREAKS ->
                "Perfect day! 🔥" to "All $done habits done — your streak stands at $streak days. Keep the flame."
            MotivationStyle.LEVELING_UP ->
                "Perfect day! ⚡" to "All $done habits done, Perfect Day bonus banked. Total: $totalXp XP and climbing."
            MotivationStyle.ACHIEVEMENTS ->
                "Perfect day! 🏆" to "All $done habits done — days like this are how badges get earned."
            MotivationStyle.QUOTES ->
                "Perfect day! ✨" to "\"We are what we repeatedly do.\" Today you did all $done. That's who you're becoming."
            else ->
                "Perfect day! 📈" to "$done of $done habits completed — a 100% day on the chart."
        }
        return Candidate(Types.CELEBRATION, Category.MOTIVATION, title, message)
    }

    private suspend fun weeklyDigest(
        today: LocalDate,
        logsByDay: Map<Long, List<com.saynedesign.habitloop.data.HabitLogEntity>>,
        todayEpoch: Long,
        habits: List<HabitEntity>,
        streak: Int
    ): Candidate? {
        if (today.dayOfWeek != DayOfWeek.SUNDAY) return null
        if (!cooldownOver(Types.WEEKLY_DIGEST, days = 6)) return null

        val weekLogs = (0L..6L).flatMap { logsByDay[todayEpoch - it].orEmpty() }
        if (weekLogs.isEmpty()) return null
        val activeDays = (0L..6L).count { logsByDay.containsKey(todayEpoch - it) }
        val bestHabitId = weekLogs.groupingBy { it.habitId }.eachCount().maxByOrNull { it.value }?.key
        val bestHabit = habits.find { it.id == bestHabitId }?.title

        val mvp = bestHabit?.let { " MVP: $it." } ?: ""
        return Candidate(
            type = Types.WEEKLY_DIGEST,
            category = Category.DIGEST,
            title = "Your week in review 🌱",
            message = "${weekLogs.size} completions across $activeDays active days.$mvp Streak: $streak days. Full report in the Growth tab."
        )
    }

    // ------------------------------------------------------------- helpers

    private suspend fun cooldownOver(type: String, days: Int): Boolean {
        val last = coachEventDao.lastEventOfType(type) ?: return true
        return System.currentTimeMillis() - last >= TimeUnit.DAYS.toMillis(days.toLong())
    }

    /** Rest-day-aware momentum, counted from yesterday backward (today may still be in progress). */
    private fun computeStreak(logDays: Set<Long>, restDayEpochs: Set<Long>, todayEpoch: Long): Int {
        var streak = 0
        var day = if (todayEpoch in logDays || todayEpoch in restDayEpochs) todayEpoch else todayEpoch - 1
        while (streak < 3650) {
            when {
                day in logDays -> { streak++; day-- }
                day in restDayEpochs -> day--
                else -> break
            }
        }
        return streak
    }
}
