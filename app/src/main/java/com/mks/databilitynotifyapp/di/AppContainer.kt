package com.mks.databilitynotifyapp.di

import android.content.Context
import com.mks.databilitynotifyapp.data.local.AppDatabase
import com.mks.databilitynotifyapp.data.repository.NotificationRepositoryImpl
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository

interface AppContainer {
    val notificationRepository: NotificationRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(database.notificationDao())
    }
}
