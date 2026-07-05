package com.saynedesign.habitloop.data

object InsightEngine {
    init {
        try {
            System.loadLibrary("insight_engine")
        } catch (e: UnsatisfiedLinkError) {
            // Log/Handle error in environments where the dynamic library is not yet compiled
            e.printStackTrace()
        }
    }

    external fun generateInsightsJson(
        userName: String,
        userDob: String,
        userHeight: Float,
        userXp: Int,
        userLevel: Int,
        userJournal: String,
        habitIds: LongArray,
        habitTitles: Array<String>,
        habitTargetValues: FloatArray,
        habitUnits: Array<String>,
        habitTypes: Array<String>,
        habitSelectedDays: Array<String>,
        habitFrequencies: Array<String>,
        habitTimesOfDay: Array<String>,
        habitCreatedEpochDays: LongArray,
        logHabitIds: LongArray,
        logDates: LongArray,
        logValues: FloatArray,
        todayEpochDay: Long
    ): String
}
