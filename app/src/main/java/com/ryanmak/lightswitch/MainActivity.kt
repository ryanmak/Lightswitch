package com.ryanmak.lightswitch

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ryanmak.lightswitch.databinding.ActivityMainBinding
import com.ryanmak.lightswitch.services.KeepOnService
import com.ryanmak.lightswitch.services.OverlayService
import com.ryanmak.lightswitch.services.OverlayService.Companion.KEY_INTENSITY_VALUE
import com.ryanmak.lightswitch.services.ServiceUtils.Companion.configKeepOnService
import com.ryanmak.lightswitch.services.ServiceUtils.Companion.configOverlayService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overlayService = Intent(this@MainActivity, OverlayService::class.java)
        keepOnService = Intent(this@MainActivity, KeepOnService::class.java)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
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
                showInfoDialog()
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

        binding.saveAndCloseButton.setOnClickListener { finish() }
        binding.dimInfoButton.setOnClickListener { it.performLongClick() }
        binding.screenOnInfoButton.setOnClickListener { it.performLongClick() }
    }

    private fun setupFlows() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.dimEnabledFlow.collect { enabled ->
                    binding.dimScreenRadioGroup.check(
                        if (enabled) R.id.onButtonDim
                        else R.id.offButtonDim
                    )

                    configOverlayService(applicationContext, enabled)
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

    private fun showInfoDialog() {
        // starting with android 11+, the user is not immediately brought to the app specific
        // permission screen; they may need additional instruction on what to do next
        val additionalInstructions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getString(R.string.permission_required_message_search)
        } else {
            ""
        }

        AlertDialog.Builder(this, R.style.LightswitchAlertDialog)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage("${getString(R.string.permission_required_message)} $additionalInstructions")
            .setPositiveButton(getString(R.string.label_ok)) { _, _ ->
                launchPermissionActivity()
            }
            .setNegativeButton(getString(R.string.label_cancel)) { _, _ ->
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
            //.setNeutralButton(getString(R.string.label_info)) { _, _ ->
            // TODO how technical do we want to be???
            //}
            .create()
            .show()
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
            startService(overlayService)
        } else {
            stopService(overlayService)
        }
    }

    private fun canKeepScreenOn(show: Boolean) {
        if (show) {
            configKeepOnService(applicationContext, true)
            startService(keepOnService)
        } else {
            configKeepOnService(applicationContext, false)
            stopService(keepOnService)
        }
    }
}