package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.GroupRepository
import com.groupflow.app.data.repository.TaskRepository
import com.groupflow.app.data.repository.ReminderRepository
import com.groupflow.app.data.local.entity.Group
import com.groupflow.app.data.local.entity.Task
import com.groupflow.app.data.local.entity.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(
    private val groupRepository: GroupRepository,
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // For now, use placeholder data since we don't have a real user ID
                // This will be updated when authentication is implemented
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groups = emptyList(),
                    tasks = emptyList(),
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
}
