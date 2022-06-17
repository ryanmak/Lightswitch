package com.ryanmak.lightswitch.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.core.app.NotificationCompat
import com.ryanmak.lightswitch.R


class KeepOnService : Service() {

    private lateinit var wakeLock: WakeLock

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK,
        "${applicationInfo.loadLabel(packageManager)}:KeepScreenOn"
        )
        wakeLock.acquire()

        startScreenOnService()
    }

    override fun onDestroy() {
        wakeLock.release()
        super.onDestroy()
    }

    private fun startScreenOnService() {
        val channelId = "keep_on_service"
        val channelName = "Background Service 1"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_bulb_lighting)
            .setContentTitle(getString(R.string.screen_on_notification_title))
            .setContentText(getString(R.string.screen_on_notification_body))
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(3, notification)
    }
}