package com.ryanmak.lightswitch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ryanmak.lightswitch.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private val activityLauncher = registerForActivityResult(StartActivityForResult()) {
        if (!Settings.canDrawOverlays(this)) {
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

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        setupListeners()
        setupFlows()
    }

    private fun setupListeners() {
        binding.dimScreenRadioGroup.setOnCheckedChangeListener { radioGroup, id ->
            val enabled = (id == R.id.onButtonDim)
            if (enabled && !Settings.canDrawOverlays(this)) {
                radioGroup.check(R.id.offButtonDim)
                launchPermissionActivity()
                return@setOnCheckedChangeListener
            }

            binding.dimIcon.visibility = if (enabled) View.VISIBLE else View.GONE
            binding.slider.isEnabled = enabled
            canShowOverlay(enabled)
        }

        binding.slider.addOnChangeListener { _, value, _ ->
            viewModel.setDimIntensity(value)
            binding.intensityCounter.text = value.toString().dropLast(2)
        }

        binding.screenOnRadioGroup.setOnCheckedChangeListener { _, id ->
            val enabled = id == R.id.onButtonScreenOn
            viewModel.setScreenOnEnabled(enabled)
            binding.screenOnIcon.visibility = if (enabled) View.VISIBLE else View.GONE
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
                    binding.intensityCounter.text = value.toString().dropLast(2)
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

    private fun launchPermissionActivity() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )

        activityLauncher.launch(intent)
    }

    private fun canShowOverlay(show: Boolean) {
        if (show) {

        }
    }
}