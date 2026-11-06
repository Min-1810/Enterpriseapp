package com.fpt.enterpriseapp.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.fpt.enterpriseapp.R

object NotificationHelper {
    fun show(context: Context, id: Int, channel: String, title: String, content: String) {
        val noti = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, noti)
    }
}