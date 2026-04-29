package com.groupflow.app.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log

class SpeechRecognitionHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "SpeechRecognition"
        const val SPEECH_REQUEST_CODE = 1001
    }
    
    /**
     * Create the speech recognition intent that should be launched via ActivityForResult
     * This is the most reliable way to do speech recognition on Android
     */
    fun createSpeechIntent(language: String = "en-US"): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your reminder...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }
    
    /**
     * Process the result from the speech recognition activity
     * Returns the recognized text or null if nothing was recognized
     */
    fun processResult(resultCode: Int, data: Intent?): String? {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val result = matches[0]
                Log.d(TAG, "Speech recognized: $result")
                return result
            }
        } else {
            Log.e(TAG, "Speech recognition failed or cancelled. Result code: $resultCode")
        }
        return null
    }
    
    /**
     * Check if speech recognition is available on this device
     */
    fun isAvailable(): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        return intent.resolveActivity(context.packageManager) != null
    }
}
