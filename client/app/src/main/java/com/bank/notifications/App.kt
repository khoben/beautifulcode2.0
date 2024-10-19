package com.bank.notifications

import android.app.Application
import com.bank.notifications.di.AppContainer
import com.bank.notifications.di.DiContainer
import timber.log.Timber

open class App : Application() {

    open val appContainer: DiContainer by lazy(LazyThreadSafetyMode.NONE) {
        AppContainer(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        appContainer.startup.start(applicationContext)
    }
}