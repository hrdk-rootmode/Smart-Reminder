package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.GroupRepository
import com.groupflow.app.data.local.entity.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val error: String? = null
)

class GroupsViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // For now, use placeholder data since we don't have a real user ID
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groups = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createGroup(name: String, description: String) {
        viewModelScope.launch {
            try {
                val group = Group(
                    groupId = java.util.UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    createdBy = "current_user_id", // Will be updated with real user ID
                    createdAt = System.currentTimeMillis()
                )
                groupRepository.insertGroup(group)
                loadGroups()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
