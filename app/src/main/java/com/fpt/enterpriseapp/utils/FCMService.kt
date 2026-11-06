package com.fpt.enterpriseapp.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.show(
            this,
            System.currentTimeMillis().toInt(),
            "system",
            message.notification?.title ?: "FPT Enterprise",
            message.notification?.body ?: ""
        )
    }
    override fun onNewToken(token: String) { super.onNewToken(token) }
}