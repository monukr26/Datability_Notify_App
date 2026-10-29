package com.mks.databilitynotifyapp.domain.repository

import com.mks.databilitynotifyapp.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<NotificationItem>>
    fun getNotificationById(id: String): Flow<NotificationItem?>
    fun getUnreadCount(): Flow<Int>
    suspend fun saveNotification(notification: NotificationItem): Boolean
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun clearAll()
}
