package com.ryanmak.lightswitch.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

class ServiceUtils {
    companion object {
        /**
         * Enables or disables the app's ability to start the Overlay service. This is needed because
         * the app will always try to run the service on startup, so we only want to allow the service
         * to run if the setting is enabled.
         *
         * @param enabled If true, allow the service to run. If false, prevent the service from running
         */
        fun configOverlayService(context: Context, enabled: Boolean) {
            val component = ComponentName(context, OverlayService::class.java)
            val pm: PackageManager = context.packageManager
            pm.setComponentEnabledSetting(
                component,
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        fun configKeepOnService(context: Context, enabled: Boolean) {
            val component = ComponentName(context, KeepOnService::class.java)
            val pm: PackageManager = context.packageManager
            pm.setComponentEnabledSetting(
                component,
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}