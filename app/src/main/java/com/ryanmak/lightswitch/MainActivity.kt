package com.ryanmak.lightswitch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ryanmak.lightswitch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        val DIM_ENABLED = "dim_enabled"
        val DIM_INTENSITY = "dim_intensity"
        val SCREEN_ON_ENABLED = "screen_on_enabled"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.dimScreenRadioGroup.setOnCheckedChangeListener { _, id ->
            when(id) {
                R.id.onButtonDim -> {

                }

                R.id.offButtonDim -> {

                }
            }
        }

        binding.screenOnRadioGroup.setOnCheckedChangeListener { _ , id ->
            when(id) {
                R.id.onButtonScreenOn -> {

                }

                R.id.offButtonScreenOn -> {

                }
            }
        }

        binding.slider.addOnChangeListener { slider, value, fromUser ->

        }
    }
}