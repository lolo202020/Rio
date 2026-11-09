package com.example.data.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _currentUtteranceId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentUtteranceId.value = null
                }
            })
        }
    }

    fun speak(
        text: String,
        language: String = "Hindi",
        gender: String = "Male",
        speakingStyle: String = "Natural",
        utteranceId: String = "dialogue_${System.currentTimeMillis()}"
    ) {
        if (!isInitialized || tts == null || text.isBlank()) return

        // Configure Locale based on language
        val targetLocale = when {
            language.contains("Hindi", ignoreCase = true) || language.contains("Bhojpuri", ignoreCase = true) -> {
                Locale.forLanguageTag("hi-IN")
            }
            language.contains("Bengali", ignoreCase = true) -> Locale.forLanguageTag("bn-IN")
            language.contains("Tamil", ignoreCase = true) -> Locale.forLanguageTag("ta-IN")
            language.contains("Marathi", ignoreCase = true) -> Locale.forLanguageTag("mr-IN")
            else -> Locale.forLanguageTag("en-IN")
        }

        try {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts?.language = Locale.ENGLISH
            }
        } catch (e: Exception) {
            tts?.language = Locale.ENGLISH
        }

        // Configure Pitch based on gender and character archetype
        val pitch = when {
            gender.equals("Female", ignoreCase = true) -> 1.15f
            speakingStyle.contains("Authoritative", ignoreCase = true) ||
            speakingStyle.contains("Deep", ignoreCase = true) -> 0.82f
            speakingStyle.contains("Excited", ignoreCase = true) ||
            speakingStyle.contains("Energetic", ignoreCase = true) -> 1.10f
            else -> 0.95f
        }
        tts?.setPitch(pitch)

        // Configure Speech Rate (Speed) based on speaking style
        val speechRate = when {
            speakingStyle.contains("Emotional", ignoreCase = true) ||
            speakingStyle.contains("Slow", ignoreCase = true) ||
            speakingStyle.contains("Hesitant", ignoreCase = true) -> 0.85f
            speakingStyle.contains("Fast", ignoreCase = true) ||
            speakingStyle.contains("Energetic", ignoreCase = true) -> 1.15f
            else -> 0.98f
        }
        tts?.setSpeechRate(speechRate)

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentUtteranceId.value = null
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
