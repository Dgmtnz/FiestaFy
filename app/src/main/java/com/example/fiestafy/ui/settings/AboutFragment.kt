package com.example.fiestafy.ui.settings

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fiestafy.BuildConfig
import com.example.fiestafy.databinding.FragmentAboutBinding
import kotlin.math.sqrt

class AboutFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val shakeThreshold = 80

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVersionInfo()
        setupSensor()
        setupClickListener()
    }

    private fun setupVersionInfo() {
        binding.versionText.text = "Versión ${BuildConfig.VERSION_NAME}"
    }

    private fun setupSensor() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun setupClickListener() {
        binding.mainScrollView.setOnClickListener {
            if (binding.shakeImage.visibility == View.VISIBLE) {
                binding.shakeImage.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val curTime = System.currentTimeMillis()
                if ((curTime - lastUpdate) > 100) {
                    val diffTime = curTime - lastUpdate
                    lastUpdate = curTime

                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    val speed = sqrt(
                        ((x - lastX) * (x - lastX) + 
                        (y - lastY) * (y - lastY) + 
                        (z - lastZ) * (z - lastZ)) / diffTime * 10000
                    )

                    if (speed > shakeThreshold) {
                        onShakeDetected()
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }
    }

    private fun onShakeDetected() {
        if (binding.shakeImage.visibility != View.VISIBLE) {
            binding.shakeImage.visibility = View.VISIBLE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 