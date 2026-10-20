package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class SensorOrientationData(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f, // Tilt forward/backward (elevation)
    val rollDegrees: Float = 0f, // Tilt left/right
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isAvailable: Boolean = true,
    val isSimulated: Boolean = false
)

class SensorFusionManager(context: Context) : SensorEventListener {

    private val sensorManager = try {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    } catch (_: Throwable) {
        null
    }

    private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _orientationData = MutableStateFlow(SensorOrientationData())
    val orientationData: StateFlow<SensorOrientationData> = _orientationData.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Fallback data if rotation vector is not supported
    private val gravityData = FloatArray(3)
    private val geomagneticData = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Exponential moving average filter coefficients
    private var smoothedAzimuthSin = 0.0
    private var smoothedAzimuthCos = 1.0
    private var smoothedPitch = 0f
    private var smoothedRoll = 0f
    private val alpha = 0.15f // Smoothing filter factor (0 < alpha <= 1)

    private var isSimulatedMode = false

    fun startListening() {
        val sm = sensorManager ?: return

        try {
            if (rotationVectorSensor != null) {
                sm.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                accelerometerSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
                magneticSensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            }
        } catch (_: Throwable) {}
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Throwable) {}
    }

    fun setSimulatedOrientation(heading: Float, pitch: Float, roll: Float = 0f) {
        isSimulatedMode = true
        _orientationData.value = SensorOrientationData(
            azimuthDegrees = (heading % 360f + 360f) % 360f,
            pitchDegrees = pitch.coerceIn(-90f, 90f),
            rollDegrees = roll.coerceIn(-90f, 90f),
            accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
            isAvailable = true,
            isSimulated = true
        )
    }

    fun resetSimulatedMode() {
        isSimulatedMode = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || isSimulatedMode) return

        try {
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    computeOrientation(event.accuracy)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravityData, 0, 3.coerceAtMost(event.values.size))
                    hasGravity = true
                    if (hasGeomagnetic) {
                        processFallback(event.accuracy)
                    }
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagneticData, 0, 3.coerceAtMost(event.values.size))
                    hasGeomagnetic = true
                    if (hasGravity) {
                        processFallback(event.accuracy)
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    private fun processFallback(accuracy: Int) {
        try {
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityData, geomagneticData)
            if (success) {
                computeOrientation(accuracy)
            }
        } catch (_: Throwable) {}
    }

    private fun computeOrientation(accuracy: Int) {
        try {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Raw azimuth in radians (-PI to +PI), convert to [0, 2PI)
            var rawAzimuthRad = orientationAngles[0].toDouble()
            if (!rawAzimuthRad.isFinite()) rawAzimuthRad = 0.0
            if (rawAzimuthRad < 0) rawAzimuthRad += 2 * PI

            // Smooth circular azimuth via unit vector components
            val currentSin = sin(rawAzimuthRad)
            val currentCos = cos(rawAzimuthRad)
            smoothedAzimuthSin = smoothedAzimuthSin * (1.0 - alpha) + currentSin * alpha
            smoothedAzimuthCos = smoothedAzimuthCos * (1.0 - alpha) + currentCos * alpha
            var smoothedAzimuthRad = atan2(smoothedAzimuthSin, smoothedAzimuthCos)
            if (!smoothedAzimuthRad.isFinite()) smoothedAzimuthRad = 0.0
            if (smoothedAzimuthRad < 0) smoothedAzimuthRad += 2 * PI
            val finalAzimuthDeg = Math.toDegrees(smoothedAzimuthRad).toFloat()

            // Pitch & Roll in degrees
            val rawPitchDeg = Math.toDegrees(-orientationAngles[1].toDouble()).toFloat()
            val rawRollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            val safeRawPitch = if (rawPitchDeg.isFinite()) rawPitchDeg else 0f
            val safeRawRoll = if (rawRollDeg.isFinite()) rawRollDeg else 0f

            smoothedPitch = smoothedPitch * (1f - alpha) + safeRawPitch * alpha
            smoothedRoll = smoothedRoll * (1f - alpha) + safeRawRoll * alpha

            val safeAzimuth = if (finalAzimuthDeg.isFinite()) ((finalAzimuthDeg % 360f + 360f) % 360f) else 0f
            val safePitch = if (smoothedPitch.isFinite()) smoothedPitch else 0f
            val safeRoll = if (smoothedRoll.isFinite()) smoothedRoll else 0f

            _orientationData.value = SensorOrientationData(
                azimuthDegrees = safeAzimuth,
                pitchDegrees = safePitch,
                rollDegrees = safeRoll,
                accuracy = accuracy,
                isAvailable = true,
                isSimulated = false
            )
        } catch (_: Throwable) {}
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _orientationData.value = _orientationData.value.copy(accuracy = accuracy)
    }
}
