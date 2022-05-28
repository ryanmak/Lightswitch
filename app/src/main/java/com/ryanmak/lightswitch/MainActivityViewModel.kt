package com.ryanmak.lightswitch

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val KEY_SETTINGS = "settings"
        private const val DIM_ENABLED = "dim_enabled"
        private const val DIM_INTENSITY = "dim_intensity"
        private const val SCREEN_ON_ENABLED = "screen_on_enabled"
    }

    private var sharedPrefs: SharedPreferences

    val dimEnabledFlow = MutableStateFlow(false)
    val dimIntensityFlow = MutableStateFlow(0f)
    val screenOnEnabledFlow = MutableStateFlow(false)

    init {
        sharedPrefs = app.getSharedPreferences(KEY_SETTINGS, Context.MODE_PRIVATE)
        dimEnabledFlow.value = sharedPrefs.getBoolean(DIM_ENABLED, false)
        dimIntensityFlow.value = sharedPrefs.getFloat(DIM_INTENSITY, 0f)
        screenOnEnabledFlow.value = sharedPrefs.getBoolean(SCREEN_ON_ENABLED, false)
    }

    fun setDimEnabled(value: Boolean) {
        dimEnabledFlow.value = value
        sharedPrefs.edit().putBoolean(DIM_ENABLED, value).apply()
    }

    fun setDimIntensity(value: Float) {
        dimIntensityFlow.value = value
        sharedPrefs.edit().putFloat(DIM_INTENSITY, value).apply()
    }

    fun setScreenOnEnabled(value: Boolean) {
        screenOnEnabledFlow.value = value
        sharedPrefs.edit().putBoolean(SCREEN_ON_ENABLED, value).apply()
    }
}