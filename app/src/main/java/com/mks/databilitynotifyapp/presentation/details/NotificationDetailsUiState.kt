package com.mks.databilitynotifyapp.presentation.details

import com.mks.databilitynotifyapp.domain.model.NotificationItem

sealed interface NotificationDetailsUiState {
    data object Loading : NotificationDetailsUiState
    data object NotFound : NotificationDetailsUiState
    data class Success(val notification: NotificationItem) : NotificationDetailsUiState
}

