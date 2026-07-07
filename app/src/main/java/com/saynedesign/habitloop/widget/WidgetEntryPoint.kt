package com.saynedesign.habitloop.widget

import android.content.Context
import com.saynedesign.habitloop.util.CompleteHabitUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt entry point for Glance ActionCallbacks (which are instantiated by the
 * framework and can't use constructor injection). Gives widget actions access
 * to the unified completion path so home-screen toggles award XP exactly like
 * in-app completions.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun completeHabitUseCase(): CompleteHabitUseCase
}

fun Context.widgetEntryPoint(): WidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
