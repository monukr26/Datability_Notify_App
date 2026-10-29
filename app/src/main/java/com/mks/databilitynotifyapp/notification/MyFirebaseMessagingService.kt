package com.mks.databilitynotifyapp.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mks.databilitynotifyapp.DatabilityApp
import com.mks.databilitynotifyapp.domain.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        // Ignore empty messages that contain neither data nor notification payload
        if (data.isEmpty() && notification == null) {
            Log.w(TAG, "FCM message contains neither data nor notification payload; skipping.")
            return
        }

        // 1. Resolve unique ID: prefer data["id"], fallback to remoteMessage.messageId, fallback to generated timestamp id
        val id = data["id"]?.trim()?.ifEmpty { null }
            ?: remoteMessage.messageId
            ?: "msg_${System.currentTimeMillis()}"

        // 2. Resolve Title: prefer data["title"], fallback to notification.title, fallback to default
        val title = data["title"]?.trim()?.ifEmpty { null }
            ?: notification?.title?.trim()?.ifEmpty { null }
            ?: "New Notification"

        // 3. Resolve Description: prefer data["description"], fallback to notification.body, fallback to empty string
        val description = data["description"]?.trim()
            ?: notification?.body?.trim()
            ?: ""

        // 4. Resolve Banner Image: prefer data["bannerImg"], fallback to notification.imageUrl
        val bannerImg = data["bannerImg"]?.trim()?.ifEmpty { null }
            ?: notification?.imageUrl?.toString()

        // 5. Resolve Timestamp: prefer data["timestamp"], fallback to sentTime, fallback to current time
        val rawTimestamp = data["timestamp"]?.trim()
        val timestamp = rawTimestamp?.toLongOrNull()
            ?: if (remoteMessage.sentTime > 0) remoteMessage.sentTime else System.currentTimeMillis()

        val notificationItem = NotificationItem(
            id = id,
            title = title,
            description = description,
            bannerImg = bannerImg,
            timestamp = timestamp,
            isRead = false
        )

        serviceScope.launch {
            val repository = (applicationContext as? DatabilityApp)?.container?.notificationRepository
            if (repository == null) {
                Log.e(TAG, "DatabilityApp container or NotificationRepository is unavailable.")
                return@launch
            }

            try {
                val isNew = repository.saveNotification(notificationItem)
                Log.d(TAG, "Notification id='$id' processed in Room. Newly inserted: $isNew")

                // Post system notification using required channel 'assignment'
                NotificationHelper.showNotification(applicationContext, notificationItem)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist notification id='$id': ${e.message}", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM registration token refreshed: $token")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
