package com.drivermate.ph

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAlertManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("en", "PH")
            tts?.setSpeechRate(0.95f)
            ready = true
        }
    }

    fun speak(message: String) {
        if (!ready || message.isBlank()) return

        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
            null,
            System.currentTimeMillis().toString()
        )
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
