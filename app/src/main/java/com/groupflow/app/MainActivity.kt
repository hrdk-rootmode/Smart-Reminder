package com.groupflow.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.groupflow.app.notification.NotificationHelper
import com.groupflow.app.service.FirebaseAuthService
import com.groupflow.app.ui.navigation.GroupFlowNavGraph
import com.groupflow.app.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAuthService: FirebaseAuthService
    
    // Speech result stored here for composables to read
    var speechResult: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }
    
    private val requestRecordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }
    
    // Speech recognition launcher - uses the reliable Intent-based approach
    val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                speechResult = matches[0]
                Log.d("MainActivity", "Speech recognized: ${matches[0]}")
            }
        } else {
            speechResult = null
            Log.d("MainActivity", "Speech recognition cancelled or failed")
        }
    }
    
    fun isSpeechRecognitionAvailable(): Boolean {
        val pm = packageManager
        val activities = pm.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
        )
        return activities.isNotEmpty()
    }
    
    fun launchSpeechRecognizer(language: String = "en-US") {
        if (!isSpeechRecognitionAvailable()) {
            Log.e("MainActivity", "Speech recognition not available on this device")
            speechResult = "ERROR_NOT_AVAILABLE"
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your reminder...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechResult = null  // Reset previous result
        speechRecognizerLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Request record audio permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestRecordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize Firebase Auth Service
        firebaseAuthService = FirebaseAuthService(this)
        firebaseAuthService.initialize()

        setContent {
            val currentUser by firebaseAuthService.currentUser.collectAsState(initial = null)
            val userTier = ThemeManager.getUserTier(currentUser)
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("reminder_prefs", MODE_PRIVATE) }
            
            // Theme state management
            var darkThemeEnabled by remember { mutableStateOf(prefs.getBoolean("dark_theme_enabled", false)) }
            val followSystemTheme = remember { mutableStateOf(prefs.getBoolean("follow_system_theme", true)) }
            
            // System theme detection
            val systemInDarkTheme = isSystemInDarkTheme()
            val shouldUseDarkTheme = if (followSystemTheme.value) {
                systemInDarkTheme
            } else {
                darkThemeEnabled
            }

            DisposableEffect(prefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
                    when (key) {
                        "dark_theme_enabled" -> {
                            darkThemeEnabled = sharedPrefs.getBoolean("dark_theme_enabled", false)
                        }
                        "follow_system_theme" -> {
                            followSystemTheme.value = sharedPrefs.getBoolean("follow_system_theme", true)
                        }
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            
            MaterialTheme(
                colorScheme = ThemeManager.getThemeForUser(
                    tier = userTier,
                    context = context,
                    forceDark = shouldUseDarkTheme
                )
            ) {
                GroupFlowNavGraph(firebaseAuthService = firebaseAuthService)
            }
        }
    }
}