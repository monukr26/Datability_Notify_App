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
        if (data.isEmpty()) {
            Log.w(TAG, "FCM message contains no data payload; skipping.")
            return
        }

        val id = data["id"]?.trim()
        if (id.isNullOrEmpty()) {
            Log.w(TAG, "FCM payload rejected: required 'id' field is missing or empty.")
            return
        }

        // Parse optional / fallback fields safely
        val title = data["title"]?.trim().takeUnless { it.isNullOrEmpty() } ?: "New Notification"
        val description = data["description"]?.trim().orEmpty()
        val bannerImg = data["bannerImg"]?.trim().takeUnless { it.isNullOrEmpty() }
        val rawTimestamp = data["timestamp"]?.trim()
        val timestamp = rawTimestamp?.toLongOrNull() ?: System.currentTimeMillis()

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
                Log.d(TAG, "Notification id='$id' processed. Newly inserted: $isNew")
                
                // Display Android system notification using required channel
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

