package com.ryanmak.lightswitch

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import androidx.core.app.NotificationCompat


class OverlayService : Service() {

    companion object {
        const val KEY_INTENSITY_VALUE = "intensity"
    }

    private lateinit var overlay: View
    private lateinit var wm: WindowManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // the higher the alpha (intensity) the darker the screen will be
        intent?.getFloatExtra(KEY_INTENSITY_VALUE, 0f)?.let {
            overlay.setBackgroundColor(Color.argb(it / 100, 0f, 0f, 0f))
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        wm.removeView(overlay)
        super.onDestroy()
    }

    @SuppressLint("InflateParams")
    override fun onCreate() {
        super.onCreate()

        val context = applicationContext
        val inflater = context?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlay = inflater.inflate(R.layout.fragment_overlay, null)

        val params = WindowManager.LayoutParams(
            MATCH_PARENT,
            MATCH_PARENT,
            TYPE_APPLICATION_OVERLAY,
            FLAG_NOT_TOUCHABLE or        // Ignore touch events
                    FLAG_NOT_FOCUSABLE or      // Ignore keyboard focus requests
                    FLAG_LAYOUT_NO_LIMITS or   // Ignore screen bounds part 1
                    FLAG_LAYOUT_IN_SCREEN,     // Ignore screen bounds part 2
            PixelFormat.TRANSLUCENT
        ).apply {
            // on Android 12+ alpha value can only go up to a maximum of 0.8f. Anymore and
            // any touch events are absorbed by the overlay.
            alpha = 0.8f

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                isFitInsetsIgnoringVisibility = false
                fitInsetsTypes = 0
                y = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) -1 else 0
                val metrics = wm.currentWindowMetrics
                height = metrics.bounds.height()
            }
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                val metrics = DisplayMetrics()
                wm.defaultDisplay.getRealMetrics(metrics)
                height = metrics.heightPixels + getNavigationBarHeight()
            }
        }

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

    private fun getNavigationBarHeight(): Int {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            val metrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            wm.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            if (realHeight > usableHeight) realHeight - usableHeight else 0
        } else {
            0
        }
    }
}