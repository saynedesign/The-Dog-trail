package com.saynedesign.habitloop.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper to trigger a refresh on all Glance widgets.
 * Call this whenever habit data changes in the app so widgets stay in sync.
 */
object WidgetUpdateHelper {

    fun updateAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            HabitChecklistWidget().updateAll(context)
            StreakSummaryWidget().updateAll(context)
            QuickActionsWidget().updateAll(context)
            // Quote rotates by day-of-year — the midnight refresh must reach it
            // too, otherwise it only changes when the launcher feels like it.
            InspirationWidget().updateAll(context)
        }
    }
}
