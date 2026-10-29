package com.mks.databilitynotifyapp

import android.app.Application
import com.mks.databilitynotifyapp.di.AppContainer
import com.mks.databilitynotifyapp.di.DefaultAppContainer

class DatabilityApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
