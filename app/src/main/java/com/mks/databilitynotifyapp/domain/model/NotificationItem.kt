package com.mks.databilitynotifyapp.domain.model

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val bannerImg: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)
