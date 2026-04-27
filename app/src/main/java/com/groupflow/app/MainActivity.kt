package com.groupflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.groupflow.app.ui.theme.SmartReminderTheme
import com.groupflow.app.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAuthService: FirebaseAuthService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
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

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize Firebase Auth Service
        firebaseAuthService = FirebaseAuthService(this)
        firebaseAuthService.initialize()

        setContent {
            val currentUser by firebaseAuthService.currentUser.collectAsState(initial = null)
            val userTier = ThemeManager.getUserTier(currentUser)
            val context = LocalContext.current
            
            MaterialTheme(
                colorScheme = ThemeManager.getThemeForUser(userTier, context = context)
            ) {
                GroupFlowNavGraph(firebaseAuthService = firebaseAuthService)
            }
        }
    }
}