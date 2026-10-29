package com.mks.databilitynotifyapp.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val uiState: StateFlow<InboxUiState> = notificationRepository.getAllNotifications()
        .map { notifications ->
            if (notifications.isEmpty()) {
                InboxUiState.Empty
            } else {
                InboxUiState.Success(notifications)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InboxUiState.Loading
        )

    val unreadCount: StateFlow<Int> = notificationRepository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAll()
        }
    }
}
