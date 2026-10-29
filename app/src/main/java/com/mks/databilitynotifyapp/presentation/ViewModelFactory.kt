package com.mks.databilitynotifyapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository
import com.mks.databilitynotifyapp.presentation.details.NotificationDetailsViewModel
import com.mks.databilitynotifyapp.presentation.inbox.InboxViewModel

class InboxViewModelFactory(
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            return InboxViewModel(notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class DetailsViewModelFactory(
    private val notificationId: String,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationDetailsViewModel::class.java)) {
            return NotificationDetailsViewModel(notificationId, notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

