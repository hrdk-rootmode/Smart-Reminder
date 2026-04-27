package com.groupflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FileItem(
    val id: String,
    val name: String,
    val size: String,
    val type: String,
    val isFolder: Boolean = false,
    val itemCount: Int? = null
)

data class FilesUiState(
    val isLoading: Boolean = true,
    val folders: List<FileItem> = emptyList(),
    val files: List<FileItem> = emptyList(),
    val error: String? = null
)

class FilesViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()
    
    init {
        loadFiles()
    }
    
    private fun loadFiles() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Placeholder data for files
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    folders = listOf(
                        FileItem("1", "Project Documents", "12 files", "folder", true, 12),
                        FileItem("2", "Shared Photos", "48 files", "folder", true, 48),
                        FileItem("3", "Meeting Notes", "8 files", "folder", true, 8),
                        FileItem("4", "Resources", "23 files", "folder", true, 23)
                    ),
                    files = listOf(
                        FileItem("5", "Project Proposal.pdf", "2.4 MB", "PDF"),
                        FileItem("6", "Budget Spreadsheet.xlsx", "856 KB", "Excel"),
                        FileItem("7", "Presentation.pptx", "5.1 MB", "PowerPoint"),
                        FileItem("8", "Meeting Notes.docx", "124 KB", "Word"),
                        FileItem("9", "Design Assets.zip", "45.2 MB", "ZIP")
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun createFolder(name: String) {
        viewModelScope.launch {
            try {
                val newFolder = FileItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    size = "0 files",
                    type = "folder",
                    isFolder = true,
                    itemCount = 0
                )
                _uiState.value = _uiState.value.copy(
                    folders = _uiState.value.folders + newFolder
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun uploadFile(name: String, size: String, type: String) {
        viewModelScope.launch {
            try {
                val newFile = FileItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    size = size,
                    type = type
                )
                _uiState.value = _uiState.value.copy(
                    files = _uiState.value.files + newFile
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
