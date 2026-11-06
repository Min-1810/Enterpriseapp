package com.fpt.enterpriseapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EnterpriseApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            listOf(
                Triple("deadline", "Deadline nhắc nhở", NotificationManager.IMPORTANCE_HIGH),
                Triple("customer_care", "Chăm sóc khách hàng", NotificationManager.IMPORTANCE_DEFAULT),
                Triple("system", "Hệ thống", NotificationManager.IMPORTANCE_LOW)
            ).forEach { (id, name, importance) ->
                manager.createNotificationChannel(NotificationChannel(id, name, importance))
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}