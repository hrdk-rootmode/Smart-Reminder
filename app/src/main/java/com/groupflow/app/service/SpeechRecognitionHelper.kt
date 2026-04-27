package com.groupflow.app.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SpeechRecognitionHelper(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    init {
        createSpeechRecognizer()
    }
    
    private fun createSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechRecognition", "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d("SpeechRecognition", "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Can be used for visual feedback
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Log.d("SpeechRecognition", "End of speech")
                }
                
                override fun onError(error: Int) {
                    Log.e("SpeechRecognition", "Error: $error")
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d("SpeechRecognition", "Results: $matches")
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d("SpeechRecognition", "Partial results: $matches")
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d("SpeechRecognition", "Event: $eventType")
                }
            })
        }
    }
    
    suspend fun startListening(onResult: (String) -> Unit, language: String = "en-US"): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your reminder")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechRecognition", "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d("SpeechRecognition", "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    Log.d("SpeechRecognition", "End of speech")
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        android.speech.SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected"
                        android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        android.speech.SpeechRecognizer.ERROR_SERVER -> "Server error"
                        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error: $error"
                    }
                    Log.e("SpeechRecognition", "Error: $error - $errorMessage")
                    continuation.resume(false)
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Failed to start listening", e)
                continuation.resumeWithException(e)
            }
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
    }
}
