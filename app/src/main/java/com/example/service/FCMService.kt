package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.repository.AppRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
        const val FCM_CHANNEL_ID = "FCM_Alert_Channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New device registration token obtained: $token")
        // Token typically registered to Firestore under the logged-in user profile
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Push packet incoming from source: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Chainage Navigator System Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "Updated transit operations noticed."
        val type = message.data["type"] ?: "Dispatch"

        // Inject into central repository flow for live UI lists
        AppRepository.sendLocalNotification(title, body, type)
        
        displayPushNotification(title, body)
    }

    private fun displayPushNotification(title: String, text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(manager)

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                "Chainage Navigator Dispatch Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Receives real-time loading tickets, chat highlights, and weighbridge triggers."
            }
            manager.createNotificationChannel(channel)
        }
    }
}
