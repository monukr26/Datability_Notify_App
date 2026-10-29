package com.mks.databilitynotifyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val bannerImg: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)
