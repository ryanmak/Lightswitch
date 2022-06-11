package com.ryanmak.lightswitch.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.datastore.preferences.preferencesDataStore

import com.ryanmak.lightswitch.R


/**
 * Implementation of App Widget functionality.
 */

private const val ACTION_DIM_BUTTON_CLICKED = "action.DIM_BUTTON_CLICKED"
private val Context.userPreferencesDataStore by preferencesDataStore("LightswitchDataStore")

class OnOffWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = RemoteViews(context.packageName, R.layout.on_off_widget)
        val widget = ComponentName(context, OnOffWidget::class.java)

        views.setOnClickPendingIntent(
            R.id.onOffWidgetImageView,
            getPendingSelfIntent(context, ACTION_DIM_BUTTON_CLICKED)
        )

        appWidgetManager.updateAppWidget(widget, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_DIM_BUTTON_CLICKED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val remoteViews = RemoteViews(context.packageName, R.layout.on_off_widget)
            val watchWidget = ComponentName(context, OnOffWidget::class.java)

            remoteViews.setImageViewResource(R.id.onOffWidgetImageView, R.drawable.ic_night_purple)
            appWidgetManager.updateAppWidget(watchWidget, remoteViews)
        }
    }


    private fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}