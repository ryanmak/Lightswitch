package com.ryanmak.lightswitch.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.userPreferencesDataStore by preferencesDataStore("LightswitchDataStore")

class DataStoreUtils private constructor(context: Context) {

    private val dataStore = context.userPreferencesDataStore

    companion object {
        val KEY_DIM_ENABLED = booleanPreferencesKey("dim_enabled")
        val KEY_DIM_INTENSITY = floatPreferencesKey("dim_intensity")
        val KEY_SCREEN_ON_ENABLED = booleanPreferencesKey("screen_on_enabled")

        // Singleton
        private var INSTANCE: DataStoreUtils? = null
        fun getInstance(context: Context): DataStoreUtils {
            if (INSTANCE == null) {
                INSTANCE = DataStoreUtils(context)
            }

            return INSTANCE as DataStoreUtils
        }
    }

    fun <T> edit(key: Preferences.Key<T>, newValue: T) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { it[key] = newValue }
        }
    }

    fun <T> getValueForKey(key: Preferences.Key<T>) = runBlocking { dataStore.data.first() }[key]

    val dimEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[KEY_DIM_ENABLED] ?: false }

    val dimIntensityFlow: Flow<Float> =
        dataStore.data.map { preferences -> preferences[KEY_DIM_INTENSITY] ?: 0f }

    val screenOnEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[KEY_SCREEN_ON_ENABLED] ?: false }
}