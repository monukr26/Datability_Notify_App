package com.mks.databilitynotifyapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mks.databilitynotifyapp.MainActivity
import com.mks.databilitynotifyapp.R
import com.mks.databilitynotifyapp.domain.model.NotificationItem

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    const val CHANNEL_ID = "assignment"
    const val CHANNEL_NAME = "Assignment"
    private const val CHANNEL_DESCRIPTION = "Notifications for Datability Inbox Assignment"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_ID' verified/created.")
        }
    }

    fun showNotification(context: Context, item: NotificationItem) {
        // Ensure channel exists on API 26+
        createNotificationChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot post system notification.")
                return
            }
        }

        // Tapping the notification opens MainActivity (landing on the Notification Inbox)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = item.title.ifBlank { "New Notification" }
        val description = item.description.ifBlank { "You have received a new notification." }

        // Use application launcher icon as notification icon
        val smallIcon = R.mipmap.ic_launcher

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Stable integer notification ID derived from the unique string ID
        val systemNotificationId = item.id.hashCode()

        try {
            NotificationManagerCompat.from(context).notify(systemNotificationId, builder.build())
            Log.d(TAG, "System notification posted for id='${item.id}' (hash=$systemNotificationId)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while posting notification: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error posting notification: ${e.message}", e)
        }
    }
}

