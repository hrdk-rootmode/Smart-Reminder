package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.UserRepository
import com.groupflow.app.data.local.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val notificationsEnabled: Boolean = true,
    val darkMode: String = "System default",
    val language: String = "English",
    val storageUsed: String = "2.4 GB",
    val storageTotal: String = "15 GB",
    val storagePercent: Float = 0.16f,
    val error: String? = null
)

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // For now, use placeholder data since we don't have a real user ID
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = null,
                    notificationsEnabled = true,
                    darkMode = "System default",
                    language = "English",
                    storageUsed = "2.4 GB",
                    storageTotal = "15 GB",
                    storagePercent = 0.16f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun updateDarkMode(mode: String) {
        _uiState.value = _uiState.value.copy(darkMode = mode)
    }

    fun updateLanguage(language: String) {
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Handle sign out logic here
                // For now, this is a placeholder
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
