package com.mks.databilitynotifyapp

import android.app.Application
import com.mks.databilitynotifyapp.di.AppContainer
import com.mks.databilitynotifyapp.di.DefaultAppContainer
import com.mks.databilitynotifyapp.notification.NotificationHelper

class DatabilityApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannel(this)
    }
}

