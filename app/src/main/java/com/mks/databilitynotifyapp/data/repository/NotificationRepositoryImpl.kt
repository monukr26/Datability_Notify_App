package com.mks.databilitynotifyapp.data.repository

import com.mks.databilitynotifyapp.data.local.NotificationDao
import com.mks.databilitynotifyapp.data.local.NotificationEntity
import com.mks.databilitynotifyapp.domain.model.NotificationItem
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getAllNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getNotificationById(id: String): Flow<NotificationItem?> {
        return notificationDao.getNotificationById(id).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun saveNotification(notification: NotificationItem): Boolean {
        val insertedRowId = notificationDao.insertNotification(notification.toEntity())
        return insertedRowId != -1L
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    private fun NotificationEntity.toDomainModel(): NotificationItem {
        return NotificationItem(
            id = id,
            title = title,
            description = description,
            bannerImg = bannerImg,
            timestamp = timestamp,
            isRead = isRead
        )
    }

    private fun NotificationItem.toEntity(): NotificationEntity {
        return NotificationEntity(
            id = id,
            title = title,
            description = description,
            bannerImg = bannerImg,
            timestamp = timestamp,
            isRead = isRead
        )
    }
}
