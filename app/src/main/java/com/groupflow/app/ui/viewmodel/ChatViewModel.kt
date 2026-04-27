package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.groupflow.app.data.repository.MessageRepository
import com.groupflow.app.data.local.entity.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val error: String? = null
)

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val groupId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load messages for the group
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val message = Message(
                    messageId = java.util.UUID.randomUUID().toString(),
                    groupId = groupId,
                    senderId = "current_user_id",
                    senderName = "User",
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
                messageRepository.insertMessage(message)
                loadMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
