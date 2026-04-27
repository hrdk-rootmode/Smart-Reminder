package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.ReminderRepository
import com.groupflow.app.data.local.entity.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RemindersUiState(
    val isLoading: Boolean = true,
    val reminders: List<Reminder> = emptyList(),
    val error: String? = null
)

class RemindersViewModel(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // For now, use placeholder data since we don't have a real user ID
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reminders = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createReminder(
        title: String,
        description: String,
        reminderTime: Long
    ) {
        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    reminderId = java.util.UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    userId = "current_user_id",
                    triggerTime = reminderTime,
                    createdAt = System.currentTimeMillis()
                )
                reminderRepository.insertReminder(reminder)
                loadReminders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markAsCompleted(reminderId: String) {
        viewModelScope.launch {
            try {
                reminderRepository.markAsCompleted(reminderId, System.currentTimeMillis())
                loadReminders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                reminderRepository.deleteReminder(reminder)
                loadReminders()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
