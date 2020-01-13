package com.example.background

import android.app.Application
import timber.log.Timber

class WorkerExampleApp: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}