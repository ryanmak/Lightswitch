package com.ryanmak.lightswitch

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private val KEY_DIM_ENABLED = booleanPreferencesKey("dim_enabled")
        private val KEY_DIM_INTENSITY = floatPreferencesKey("dim_intensity")
        private val KEY_SCREEN_ON_ENABLED = booleanPreferencesKey("screen_on_enabled")
    }

    private lateinit var dataStore: DataStore<Preferences>

    val dimEnabledFlow = MutableStateFlow(false)
    val dimIntensityFlow = MutableStateFlow(0f)
    val screenOnEnabledFlow = MutableStateFlow(false)

    fun setDataStore(userPreferencesDataStore: DataStore<Preferences>) {
        dataStore = userPreferencesDataStore

        viewModelScope.launch {
            dataStore.data.let {
                dimEnabledFlow.value = it.first()[KEY_DIM_ENABLED] ?: false
                dimIntensityFlow.value = it.first()[KEY_DIM_INTENSITY] ?: 0f
                screenOnEnabledFlow.value = it.first()[KEY_SCREEN_ON_ENABLED] ?: false
            }
        }
    }

    fun setDimEnabled(value: Boolean) {
        dimEnabledFlow.value = value

        viewModelScope.launch {
            dataStore.edit { preferences -> preferences[KEY_DIM_ENABLED] = value }
        }
    }

    fun setDimIntensity(value: Float) {
        dimIntensityFlow.value = value

        viewModelScope.launch {
            dataStore.edit { preferences -> preferences[KEY_DIM_INTENSITY] = value }
        }
    }

    fun setScreenOnEnabled(value: Boolean) {
        screenOnEnabledFlow.value = value

        viewModelScope.launch {
            dataStore.edit { preferences -> preferences[KEY_SCREEN_ON_ENABLED] = value }
        }
    }
}