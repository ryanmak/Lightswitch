package com.ryanmak.lightswitch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ryanmak.lightswitch.OverlayService.Companion.KEY_INTENSITY_VALUE
import com.ryanmak.lightswitch.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private val Context.userPreferencesDataStore by preferencesDataStore("LightswitchDataStore")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var overlayService: Intent
    private lateinit var keepOnService: Intent

    private var activityLauncher = registerForActivityResult(StartActivityForResult()) {
        if (!Settings.canDrawOverlays(this)) {
            // User still did not give permission
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Thank you!", Toast.LENGTH_SHORT).show()
            viewModel.setDimEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overlayService = Intent(this@MainActivity, OverlayService::class.java)
        keepOnService = Intent(this@MainActivity, KeepOnService::class.java)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.setDataStore(userPreferencesDataStore)
        setupFlows()
        setupListeners()
    }

    private fun setupListeners() {
        binding.dimScreenRadioGroup.setOnCheckedChangeListener { radioGroup, id ->
            val enabled = (id == R.id.onButtonDim)

            // Check if app has permission to draw over apps
            if (enabled && !Settings.canDrawOverlays(this)) {
                radioGroup.check(R.id.offButtonDim)
                viewModel.setDimEnabled(false)
                launchPermissionActivity()
                return@setOnCheckedChangeListener
            }

            viewModel.setDimEnabled(enabled)
            binding.dimIcon.visibility = if (enabled) View.VISIBLE else View.GONE
            binding.slider.isEnabled = enabled
            canShowOverlay(enabled)
        }

        binding.slider.addOnChangeListener { _, value, _ ->
            viewModel.setDimIntensity(value)

            // value is a float, so removing the last 2 chars removes the '.0'
            binding.intensityCounter.text = value.toString().dropLast(2)
        }

        binding.screenOnRadioGroup.setOnCheckedChangeListener { _, id ->
            val enabled = id == R.id.onButtonScreenOn
            viewModel.setScreenOnEnabled(enabled)
            binding.screenOnIcon.visibility = if (enabled) View.VISIBLE else View.GONE

            canKeepScreenOn(enabled)
        }

        binding.saveAndCloseButton.setOnClickListener {
            finish()
        }
    }

    private fun setupFlows() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.dimEnabledFlow.collect { enabled ->
                    binding.dimScreenRadioGroup.check(
                        if (enabled) R.id.onButtonDim
                        else R.id.offButtonDim
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.dimIntensityFlow.collect { value ->
                    binding.slider.value = value

                    // value is a float, so removing the last 2 chars removes the '.0'
                    binding.intensityCounter.text = value.toString().dropLast(2)

                    // A service only has one instance running at a time. By starting the same
                    // service again, we instead update the running service via Intent.putExtra
                    overlayService = Intent(this@MainActivity, OverlayService::class.java).apply {
                        putExtra(KEY_INTENSITY_VALUE, value)
                    }
                    startService(overlayService)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.screenOnEnabledFlow.collect { enabled ->
                    binding.screenOnRadioGroup.check(
                        if (enabled) R.id.onButtonScreenOn
                        else R.id.offButtonScreenOn
                    )
                }
            }
        }
    }

    /**
     * Launches a new activity that brings the user to the draw overlays permission screen. Here,
     * the user is expected to check the Draw over Apps permission for our app.
     */
    private fun launchPermissionActivity() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )

        activityLauncher.launch(intent)
    }

    private fun canShowOverlay(show: Boolean) {
        if (show) {
            configOverlayService(true)
            startService(overlayService)
        } else {
            configOverlayService(false)
            stopService(overlayService)
        }
    }

    private fun canKeepScreenOn(show: Boolean) {
        if (show) {
            configKeepOnService(true)
            startService(keepOnService)
        } else {
            configKeepOnService(false)
            stopService(keepOnService)
        }
    }

    /**
     * Enables or disables the app's ability to start the Overlay service. This is needed because
     * the app will always try to run the service on startup, so we only want to allow the service
     * to run if the setting is enabled.
     *
     * @param enabled If true, allow the service to run. If false, forbid the service from running
     */
    private fun configOverlayService(enabled: Boolean) {
        val component = ComponentName(applicationContext, OverlayService::class.java)
        val pm: PackageManager = applicationContext.packageManager
        pm.setComponentEnabledSetting(
            component,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun configKeepOnService(enabled: Boolean) {
        val component = ComponentName(applicationContext, KeepOnService::class.java)
        val pm: PackageManager = applicationContext.packageManager
        pm.setComponentEnabledSetting(
            component,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}