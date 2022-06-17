package com.ryanmak.lightswitch.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.ryanmak.lightswitch.R
import com.ryanmak.lightswitch.datastore.DataStoreUtils
import com.ryanmak.lightswitch.services.KeepOnService
import com.ryanmak.lightswitch.services.ServiceUtils

private const val ACTION_KEEP_ON_BUTTON_CLICKED = "KEEP_ON_BUTTON_CLICKED"

class KeepOnWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.keep_on_widget)
        remoteViews.setOnClickPendingIntent(
            R.id.keepOnWidgetImageView,
            getPendingSelfIntent(context, ACTION_KEEP_ON_BUTTON_CLICKED)
        )

        appWidgetIds.forEach { id ->
            val dataStore = DataStoreUtils.getInstance(context)
            val enabled = dataStore.getValueForKey(DataStoreUtils.KEY_SCREEN_ON_ENABLED) ?: false
            updateImageView(enabled, remoteViews)
            appWidgetManager.updateAppWidget(id, remoteViews)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null) return

        if (intent?.action == ACTION_KEEP_ON_BUTTON_CLICKED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widget = ComponentName(context, KeepOnWidget::class.java)
            val remoteViews = RemoteViews(context.packageName, R.layout.keep_on_widget)
            val dataStore = DataStoreUtils.getInstance(context)
            val enabled = dataStore.getValueForKey(DataStoreUtils.KEY_SCREEN_ON_ENABLED) ?: false
            dataStore.edit(DataStoreUtils.KEY_SCREEN_ON_ENABLED, !enabled)

            updateImageView(!enabled, remoteViews)
            updateService(context, !enabled)
            appWidgetManager.updateAppWidget(widget, remoteViews)
        }
    }

    private fun updateImageView(enabled: Boolean, remoteViews: RemoteViews) {
        if (enabled) {
            remoteViews.setImageViewResource(
                R.id.keepOnWidgetImageView,
                R.drawable.ic_bulb_lighting_purple
            )
        } else {
            remoteViews.setImageViewResource(
                R.id.keepOnWidgetImageView,
                R.drawable.ic_bulb_lighting
            )
        }
    }

    private fun updateService(context: Context, enabled: Boolean) {
        val overlayIntent = Intent(context, KeepOnService::class.java)

        if (enabled) {
            ServiceUtils.configOverlayService(context, true)
            context.startForegroundService(overlayIntent)
        } else {
            context.stopService(overlayIntent)
        }
    }

    private fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}