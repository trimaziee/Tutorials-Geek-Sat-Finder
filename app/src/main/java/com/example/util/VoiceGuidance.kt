package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class VoiceGuidance(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    @Volatile
    var isEnabled = true

    private var lastSpokenTime = 0L
    private var lastSpokenText = ""

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Throwable) {
            // TTS engine unavailable on emulator
            tts = null
            isReady = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                tts?.language = Locale.US
                isReady = true
            } catch (_: Throwable) {
                isReady = false
            }
        }
    }

    /**
     * Speaks guidance instruction if enough time has passed and angle has changed.
     */
    fun provideDirectionalGuidance(angleDiff: Float, isLocked: Boolean, satelliteName: String) {
        if (!isEnabled || !isReady || tts == null) return

        val now = System.currentTimeMillis()
        if (now - lastSpokenTime < 3500) return // Throttle speech to every 3.5s

        val textToSpeak = when {
            isLocked -> "Target locked on $satelliteName! Hold position."
            abs(angleDiff) <= 3f -> "Almost there, fine tune slowly."
            angleDiff > 5f -> {
                val degrees = angleDiff.roundToInt()
                "Turn right $degrees degrees"
            }
            angleDiff < -5f -> {
                val degrees = abs(angleDiff).roundToInt()
                "Turn left $degrees degrees"
            }
            else -> return
        }

        if (textToSpeak != lastSpokenText || now - lastSpokenTime > 8000) {
            lastSpokenText = textToSpeak
            lastSpokenTime = now
            try {
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "sat_guidance_${System.currentTimeMillis()}")
            } catch (_: Throwable) {}
        }
    }

    fun speak(text: String) {
        if (!isEnabled || !isReady || tts == null) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "sat_prompt_${System.currentTimeMillis()}")
        } catch (_: Throwable) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {}
        tts = null
        isReady = false
    }
}
