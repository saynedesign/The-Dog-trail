package com.codesmithslabs.thedogtail.widget

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
            // InspirationWidget rotates by day, no data-driven update needed
        }
    }
}
