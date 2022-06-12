package com.ryanmak.lightswitch.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import com.ryanmak.lightswitch.OverlayService
import com.ryanmak.lightswitch.R
import com.ryanmak.lightswitch.ServiceUtils
import com.ryanmak.lightswitch.datastore.DataStoreUtils
import com.ryanmak.lightswitch.datastore.DataStoreUtils.Companion.KEY_DIM_ENABLED

private const val ACTION_DIM_BUTTON_CLICKED = "DIM_BUTTON_CLICKED"

class OnOffWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (!Settings.canDrawOverlays(context)) return

        val remoteViews = RemoteViews(context.packageName, R.layout.on_off_widget)
        remoteViews.setOnClickPendingIntent(
            R.id.onOffWidgetImageView,
            getPendingSelfIntent(context, ACTION_DIM_BUTTON_CLICKED)
        )

        appWidgetIds.forEach { id ->
            val dataStore = DataStoreUtils.getInstance(context)
            val enabled = dataStore.getValueForKey(KEY_DIM_ENABLED) ?: false
            updateImageView(enabled, remoteViews)
            appWidgetManager.updateAppWidget(id, remoteViews)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null) return
        if (!Settings.canDrawOverlays(context)) return

        if (intent?.action == ACTION_DIM_BUTTON_CLICKED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widget = ComponentName(context, OnOffWidget::class.java)
            val remoteViews = RemoteViews(context.packageName, R.layout.on_off_widget)
            val dataStore = DataStoreUtils.getInstance(context)
            val enabled = dataStore.getValueForKey(KEY_DIM_ENABLED) ?: false
            dataStore.edit(KEY_DIM_ENABLED, !enabled)

            updateImageView(!enabled, remoteViews)
            updateService(context, !enabled)
            appWidgetManager.updateAppWidget(widget, remoteViews)
        }
    }

    private fun updateImageView(enabled: Boolean, remoteViews: RemoteViews) {
        if (enabled) {
            remoteViews.setImageViewResource(
                R.id.onOffWidgetImageView,
                R.drawable.ic_night_purple
            )
        } else {
            remoteViews.setImageViewResource(
                R.id.onOffWidgetImageView,
                R.drawable.ic_night
            )
        }
    }

    private fun updateService(context: Context, enabled: Boolean) {
        val overlayIntent = Intent(context, OverlayService::class.java)

        if (enabled) {
            ServiceUtils.configOverlayService(context, true)
            val intensity =
                DataStoreUtils.getInstance(context).getValueForKey(DataStoreUtils.KEY_DIM_INTENSITY)
                    ?: 0f
            overlayIntent.putExtra(OverlayService.KEY_INTENSITY_VALUE, intensity)
            context.startForegroundService(overlayIntent)
        } else {
            context.stopService(overlayIntent)
        }
    }

    private fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (FLAG_MUTABLE or FLAG_UPDATE_CURRENT) else FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}