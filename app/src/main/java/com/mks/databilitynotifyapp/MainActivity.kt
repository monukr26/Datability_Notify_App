package com.mks.databilitynotifyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.mks.databilitynotifyapp.domain.model.NotificationItem
import com.mks.databilitynotifyapp.domain.repository.NotificationRepository
import com.mks.databilitynotifyapp.presentation.navigation.NavGraph
import com.mks.databilitynotifyapp.ui.theme.DatabilityNotifyAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as DatabilityApp).container

        // Handle notification clicked from system tray (e.g. background FCM delivery)
        handleNotificationIntent(intent, appContainer.notificationRepository)

        // Retrieve and log FCM registration token on launch
        fetchFcmToken()

        setContent {
            DatabilityNotifyAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val appContainer = (application as DatabilityApp).container
        handleNotificationIntent(intent, appContainer.notificationRepository)
    }

    private fun handleNotificationIntent(intent: Intent?, repository: NotificationRepository) {
        val extras = intent?.extras ?: return

        // Check if intent was launched from an FCM notification
        val id = extras.getString("id")?.trim()?.ifEmpty { null }
            ?: extras.getString("google.message_id")
            ?: return

        val title = extras.getString("title")?.trim()?.ifEmpty { null }
            ?: extras.getString("gcm.notification.title")?.trim()?.ifEmpty { null }
            ?: "New Notification"

        val description = extras.getString("description")?.trim()
            ?: extras.getString("gcm.notification.body")?.trim()
            ?: ""

        val bannerImg = extras.getString("bannerImg")?.trim()?.ifEmpty { null }
            ?: extras.getString("gcm.notification.image")?.trim()?.ifEmpty { null }

        val rawTimestamp = extras.getString("timestamp")?.trim()
        val timestamp = rawTimestamp?.toLongOrNull() ?: System.currentTimeMillis()

        val notificationItem = NotificationItem(
            id = id,
            title = title,
            description = description,
            bannerImg = bannerImg,
            timestamp = timestamp,
            isRead = false
        )

        lifecycleScope.launch(Dispatchers.IO) {
            repository.saveNotification(notificationItem)
            Log.d("MainActivity", "Saved background tapped notification id='$id' to Room.")
        }
    }

    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "==================================================")
                Log.d("FCM_TOKEN", "CURRENT FCM TOKEN: $token")
                Log.d("FCM_TOKEN", "==================================================")
            } else {
                Log.e("FCM_TOKEN", "Failed to retrieve FCM token: ${task.exception?.message}", task.exception)
            }
        }
    }
}
