package com.mks.databilitynotifyapp.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationDetailsViewModel(
    private val notificationId: String,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationDetailsUiState>(NotificationDetailsUiState.Loading)
    val uiState: StateFlow<NotificationDetailsUiState> = _uiState.asStateFlow()

    private var hasMarkedAsRead = false

    init {
        loadNotification()
    }

    private fun loadNotification() {
        viewModelScope.launch {
            notificationRepository.getNotificationById(notificationId)
                .collect { item ->
                    if (item == null) {
                        _uiState.value = NotificationDetailsUiState.NotFound
                    } else {
                        _uiState.value = NotificationDetailsUiState.Success(item)
                        if (!hasMarkedAsRead && !item.isRead) {
                            hasMarkedAsRead = true
                            notificationRepository.markAsRead(notificationId)
                        }
                    }
                }
        }
    }
}

