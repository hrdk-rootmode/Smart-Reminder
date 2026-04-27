package com.groupflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.groupflow.app.service.FirebaseAuthService
import com.groupflow.app.ui.navigation.GroupFlowNavGraph
import com.groupflow.app.ui.theme.SmartReminderTheme

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAuthService: FirebaseAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Auth Service
        firebaseAuthService = FirebaseAuthService(this)
        firebaseAuthService.initialize()

        setContent {
            SmartReminderTheme {
                GroupFlowNavGraph(firebaseAuthService = firebaseAuthService)
            }
        }
    }
}