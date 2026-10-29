package com.mks.databilitynotifyapp.presentation.inbox

import com.mks.databilitynotifyapp.domain.model.NotificationItem

sealed interface InboxUiState {
    data object Loading : InboxUiState
    data object Empty : InboxUiState
    data class Success(val notifications: List<NotificationItem>) : InboxUiState
}

