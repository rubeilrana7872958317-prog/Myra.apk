package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceAssistantManager(
    private val context: Context,
    private val onListeningStateChange: (Boolean) -> Unit,
    private val onRmsChange: (Float) -> Unit,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onSpeakingStateChange: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    var currentLanguageCode = "en" // "bn", "hi", "en"
    var speechPitch = 1.0f
    var speechSpeed = 1.0f

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            updateTtsLanguage(currentLanguageCode)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChange(true)
                }
                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChange(false)
                }
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChange(false)
                }
            })
        }
    }

    fun updateTtsLanguage(languageCode: String) {
        currentLanguageCode = languageCode
        if (!isTtsReady) return
        val locale = when (languageCode) {
            "bn" -> Locale("bn", "BD")
            "hi" -> Locale("hi", "IN")
            else -> Locale.US
        }
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English if regional language pack is missing on device
            textToSpeech?.setLanguage(Locale.US)
        }
        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechSpeed)
    }

    fun speak(text: String, languageCode: String = currentLanguageCode) {
        if (!isTtsReady || textToSpeech == null) return
        updateTtsLanguage(languageCode)
        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechSpeed)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MYRA_SPEAK_${System.currentTimeMillis()}")
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "MYRA_SPEAK")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        onSpeakingStateChange(false)
    }

    fun startListening(languageCode: String = currentLanguageCode) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        stopSpeaking()
        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningStateChange(true)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize -2dB..10dB to 0f..1f
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    onRmsChange(normalized)
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningStateChange(false)
                }
                override fun onError(error: Int) {
                    onListeningStateChange(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission required"
                        else -> "Speech recognition error ($error)"
                    }
                    onError(message)
                }
                override fun onResults(results: Bundle?) {
                    onListeningStateChange(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onResult(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onRmsChange(0.7f)
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val speechLocale = when (languageCode) {
            "bn" -> "bn-BD"
            "hi" -> "hi-IN"
            else -> "en-US"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLocale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to start speech recognition")
            onListeningStateChange(false)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        onListeningStateChange(false)
        onRmsChange(0f)
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
