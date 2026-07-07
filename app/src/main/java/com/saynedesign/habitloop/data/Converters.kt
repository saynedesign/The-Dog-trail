package com.saynedesign.habitloop.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPrimaryGoal(value: PrimaryGoal?): String? = value?.name
    @TypeConverter
    fun toPrimaryGoal(value: String?): PrimaryGoal? = value?.let { 
        try { PrimaryGoal.valueOf(it) } catch (e: Exception) { PrimaryGoal.FITNESS }
    }

    @TypeConverter
    fun fromProductivityTime(value: ProductivityTime?): String? = value?.name
    @TypeConverter
    fun toProductivityTime(value: String?): ProductivityTime? = value?.let { 
        try { ProductivityTime.valueOf(it) } catch (e: Exception) { ProductivityTime.MORNING }
    }

    @TypeConverter
    fun fromMotivationStyle(value: MotivationStyle?): String? = value?.name
    @TypeConverter
    fun toMotivationStyle(value: String?): MotivationStyle? = value?.let { 
        try { MotivationStyle.valueOf(it) } catch (e: Exception) { MotivationStyle.SEEING_PROGRESS }
    }

    @TypeConverter
    fun fromExperienceLevel(value: ExperienceLevel?): String? = value?.name
    @TypeConverter
    fun toExperienceLevel(value: String?): ExperienceLevel? = value?.let { 
        try { ExperienceLevel.valueOf(it) } catch (e: Exception) { ExperienceLevel.BEGINNER }
    }

    @TypeConverter
    fun fromWeekStartsOn(value: WeekStartsOn?): String? = value?.name
    @TypeConverter
    fun toWeekStartsOn(value: String?): WeekStartsOn? = value?.let { 
        try { WeekStartsOn.valueOf(it) } catch (e: Exception) { WeekStartsOn.MONDAY }
    }

    @TypeConverter
    fun fromUnitSystem(value: UnitSystem?): String? = value?.name
    @TypeConverter
    fun toUnitSystem(value: String?): UnitSystem? = value?.let { 
        try { UnitSystem.valueOf(it) } catch (e: Exception) { UnitSystem.METRIC }
    }
}
