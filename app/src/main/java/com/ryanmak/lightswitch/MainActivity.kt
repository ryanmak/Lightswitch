package com.ryanmak.lightswitch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ryanmak.lightswitch.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        setupListeners()
        setupFlows()
    }

    private fun setupListeners() {
        binding.dimScreenRadioGroup.setOnCheckedChangeListener { _, id ->
            viewModel.setDimEnabled(id == R.id.onButtonDim)
        }

        binding.slider.addOnChangeListener { _, value, _ ->
            viewModel.setDimIntensity(value)
        }

        binding.screenOnRadioGroup.setOnCheckedChangeListener { _, id ->
            viewModel.setScreenOnEnabled(id == R.id.onButtonScreenOn)
        }
    }

    private fun setupFlows() {
        lifecycleScope.launch {
            viewModel.dimEnabledFlow.collect { enabled ->
                binding.dimScreenRadioGroup.check(
                    if (enabled) R.id.onButtonDim
                    else R.id.offButtonDim
                )
            }
        }

        lifecycleScope.launch {
            viewModel.dimIntensityFlow.collect { value ->
                binding.slider.value = value
            }
        }

        lifecycleScope.launch {
            viewModel.screenOnEnabledFlow.collect { enabled ->
                binding.screenOnRadioGroup.check(
                    if (enabled) R.id.onButtonScreenOn
                    else R.id.offButtonScreenOn
                )
            }
        }
    }
}