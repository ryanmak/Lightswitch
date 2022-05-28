package com.ryanmak.lightswitch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationCompat


class OverlayService : Service() {

    private lateinit var overlay: View
    private lateinit var wm: WindowManager
    private var intensity: Float? = 0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intensity = intent?.getFloatExtra("intensity", 0f)

        intensity?.let {
            overlay.setBackgroundColor(Color.argb(it/100, 0f, 0f, 0f))
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        wm.removeView(overlay)
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()

        val context = applicationContext
        val inflater = context?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { alpha = 0.8f }

        overlay = inflater.inflate(R.layout.fragment_overlay, null)
        wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.addView(overlay, params)

        startOverlayService()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startOverlayService() {
        val channelId = "overlay_service"
        val channelName = "Background Service"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Service running")
            .setContentText("Displaying over other apps")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }
}