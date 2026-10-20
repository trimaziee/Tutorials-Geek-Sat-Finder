package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioBeeper {

    private var audioTrack: AudioTrack? = null
    private var beeperJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val sampleRate = 44100
    private var isPlaying = false

    @Volatile
    var isEnabled = true

    init {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize > 0) {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                    audioTrack?.play()
                    isPlaying = true
                }
            }
        } catch (_: Throwable) {
            // AudioTrack unavailable or muted in headless emulator
            isPlaying = false
            audioTrack = null
        }
    }

    /**
     * Updates beeper frequency and pulse rate based on angle delta (degrees).
     * @param angleDelta Absolute degrees away from target (0 = dead center lock).
     * @param isLocked True if both azimuth and elevation are locked.
     */
    fun updateAlignmentStatus(angleDelta: Float, isLocked: Boolean) {
        if (!isEnabled || !isPlaying || audioTrack == null) {
            beeperJob?.cancel()
            return
        }

        beeperJob?.cancel()
        beeperJob = scope.launch {
            try {
                if (isLocked) {
                    // Continuous locked tone (2400 Hz)
                    playTone(2400.0, 180)
                    delay(20)
                } else if (angleDelta < 25f) {
                    // Modulate pulse interval and pitch based on closeness:
                    val factor = (angleDelta / 25f).coerceIn(0f, 1f)
                    val toneFreq = 1800.0 - (factor * 900.0)
                    val pulseInterval = (50L + (factor * 350L)).toLong()
                    val toneDurationMs = 40

                    while (isActive) {
                        playTone(toneFreq, toneDurationMs)
                        delay(pulseInterval)
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun stop() {
        beeperJob?.cancel()
    }

    private fun playTone(frequency: Double, durationMs: Int) {
        val track = audioTrack ?: return
        try {
            if (track.state != AudioTrack.STATE_INITIALIZED) return
            val numSamples = (durationMs * sampleRate / 1000)
            val buffer = ShortArray(numSamples)
            val angularFreq = 2.0 * PI * frequency / sampleRate

            for (i in 0 until numSamples) {
                val envelope = when {
                    i < 100 -> i / 100.0
                    i > numSamples - 100 -> (numSamples - i) / 100.0
                    else -> 1.0
                }
                val sampleVal = (sin(angularFreq * i) * 32767 * 0.4 * envelope).toInt()
                buffer[i] = sampleVal.toShort()
            }
            track.write(buffer, 0, numSamples)
        } catch (_: Throwable) {
            // Audio buffer error
        }
    }

    fun release() {
        beeperJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Throwable) {}
        audioTrack = null
        isPlaying = false
    }
}
