package com.ryanmak.lightswitch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryanmak.lightswitch.datastore.DataStoreUtils
import com.ryanmak.lightswitch.datastore.DataStoreUtils.Companion.KEY_DIM_ENABLED
import com.ryanmak.lightswitch.datastore.DataStoreUtils.Companion.KEY_DIM_INTENSITY
import com.ryanmak.lightswitch.datastore.DataStoreUtils.Companion.KEY_SCREEN_ON_ENABLED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityViewModel(app: Application) : AndroidViewModel(app) {

    private var dataStore: DataStoreUtils = DataStoreUtils.getInstance(app)

    lateinit var dimEnabledFlow: Flow<Boolean>
    lateinit var dimIntensityFlow: Flow<Float>
    lateinit var screenOnEnabledFlow: Flow<Boolean>

    init {
        viewModelScope.launch {
            dimEnabledFlow = dataStore.dimEnabledFlow
            dimIntensityFlow = dataStore.dimIntensityFlow
            screenOnEnabledFlow = dataStore.screenOnEnabledFlow
        }
    }

    fun setDimEnabled(value: Boolean) {
        dataStore.edit(KEY_DIM_ENABLED, value)
    }

    fun setDimIntensity(value: Float) {
        dataStore.edit(KEY_DIM_INTENSITY, value)
    }

    fun setScreenOnEnabled(value: Boolean) {
        dataStore.edit(KEY_SCREEN_ON_ENABLED, value)
    }
}