package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.TaskRepository
import com.groupflow.app.data.local.entity.Task
import com.groupflow.app.data.local.entity.TaskPriority
import com.groupflow.app.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val selectedFilter: String = "All",
    val error: String? = null
)

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // For now, use placeholder data since we don't have a real user ID
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        // Reload tasks with filter
        loadTasks()
    }

    fun createTask(title: String, description: String, dueDate: Long, priority: TaskPriority) {
        viewModelScope.launch {
            try {
                val task = Task(
                    taskId = java.util.UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    groupId = "default_group",
                    createdBy = "current_user_id",
                    assignedTo = "current_user_id",
                    dueDate = dueDate,
                    priority = priority,
                    status = TaskStatus.TODO,
                    createdAt = System.currentTimeMillis()
                )
                taskRepository.insertTask(task)
                loadTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            try {
                // Get the task and update its status
                // For now, this is a placeholder
                loadTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
