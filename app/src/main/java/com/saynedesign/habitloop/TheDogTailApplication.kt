package com.saynedesign.habitloop

import android.app.Application
import com.saynedesign.habitloop.receivers.MidnightRefreshReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TheDogTailApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Keep widgets rolling over to the new day even if the app isn't opened.
        MidnightRefreshReceiver.schedule(this)
    }
}
