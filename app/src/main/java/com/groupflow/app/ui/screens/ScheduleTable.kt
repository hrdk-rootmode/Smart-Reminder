package com.groupflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.ui.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

/**
 * Screen 3: Monthly Schedule Table - Habit/Routine Tracker
 * User creates time-based routines and tracks daily completion with checkboxes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScheduleScreen(
    reminders: List<Reminder>,
    viewModel: ReminderViewModel
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showAddRowDialog by remember { mutableStateOf(false) }
    var showEditRowDialog by remember { mutableStateOf(false) }
    var editingRowIndex by remember { mutableStateOf(-1) }
    
    // Schedule rows - each row has time range and title
    data class ScheduleRow(
        val id: String = UUID.randomUUID().toString(),
        val startTime: String,
        val endTime: String,
        val title: String,
        val description: String = "",
        val completedDates: MutableSet<Int> = mutableSetOf() // Day of month
    )
    
    var scheduleRows by remember {
        mutableStateOf(listOf(
            ScheduleRow(
                startTime = "08:00",
                endTime = "10:00",
                title = "Morning Workout",
                description = "Exercise and stretching",
                completedDates = mutableSetOf(1, 2, 3) // Example completed dates
            )
        ))
    }
    
    // Calculate progress
    val daysInMonth = currentMonth.lengthOfMonth()
    val totalCells = scheduleRows.size * daysInMonth
    val completedCells = scheduleRows.sumOf { it.completedDates.size }
    val completionRate = if (totalCells > 0) (completedCells.toFloat() / totalCells * 100).toInt() else 0
    
    // Motivational messages
    val motivationMessage = when {
        completionRate >= 90 -> "🔥 Outstanding! You're crushing it!"
        completionRate >= 75 -> "⭐ Excellent consistency! Keep going!"
        completionRate >= 60 -> "💪 Great progress! You're doing well!"
        completionRate >= 40 -> "👍 Good effort! Stay consistent!"
        completionRate >= 20 -> "🌱 Keep building the habit!"
        else -> "💡 Start small, stay consistent!"
    }
    
    // Zoom state
    var scale by remember { mutableStateOf(1f) }
    val minScale = 0.5f
    val maxScale = 2f
    
    // Detect screen orientation and size
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp
    
    // Adjust cell sizes based on screen
    val indexWidth = if (isTablet) 50.dp else 40.dp
    val timeWidth = if (isTablet) 120.dp else 100.dp
    val titleWidth = if (isTablet) 200.dp else 150.dp
    val dateWidth = if (isTablet) 60.dp else 50.dp
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Header with month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Schedule Table (${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "← Swipe to Calendar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress and Motivation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    completionRate >= 75 -> Color(0xFFE8F5E9)
                    completionRate >= 40 -> Color(0xFFFFF3E0)
                    else -> Color(0xFFFFEBEE)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            motivationMessage,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$completedCells of $totalCells tasks completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "$completionRate%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            completionRate >= 75 -> Color(0xFF4CAF50)
                            completionRate >= 40 -> Color(0xFFFF9800)
                            else -> Color(0xFFEF5350)
                        }
                    )
                }
                LinearProgressIndicator(
                    progress = { completionRate / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        completionRate >= 75 -> Color(0xFF4CAF50)
                        completionRate >= 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFEF5350)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Zoom controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { scale = (scale - 0.1f).coerceAtLeast(minScale) },
                    enabled = scale > minScale
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                }
                Text(
                    "${(scale * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                IconButton(
                    onClick = { scale = (scale + 0.1f).coerceAtMost(maxScale) },
                    enabled = scale < maxScale
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Zoom In", modifier = Modifier.size(24.dp))
                }
            }
            
            Button(
                onClick = { showAddRowDialog = true },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Row", style = MaterialTheme.typography.labelMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Scrollable Table with synchronized scrolling
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Shared horizontal scroll state for synchronized scrolling
            val horizontalScrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        transformOrigin = TransformOrigin(0f, 0f)
                    )
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Fixed columns (Index, Time, Title)
                    Row {
                        TableHeaderCell("#", indexWidth)
                        TableHeaderCell("Time", timeWidth)
                        TableHeaderCell("Title / Description", titleWidth)
                    }
                    
                    // Scrollable date columns
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        repeat(daysInMonth) { day ->
                            val dayNum = day + 1
                            val date = currentMonth.atDay(dayNum)
                            val dayOfWeek = date.dayOfWeek.name.take(1)
                            val isToday = dayNum == LocalDate.now().dayOfMonth && currentMonth == YearMonth.now()
                            
                            TableHeaderCell(
                                text = "$dayOfWeek\n$dayNum",
                                width = dateWidth,
                                isToday = isToday
                            )
                        }
                    }
                }
                
                // Schedule rows (scrollable vertically)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    scheduleRows.forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Fixed columns
                            Row {
                                // Index
                                TableCell(
                                    content = {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    width = indexWidth
                                )
                                
                                // Time
                                TableCell(
                                    content = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                editingRowIndex = index
                                                showEditRowDialog = true
                                            }
                                        ) {
                                            Text(
                                                row.startTime,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "to",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp
                                            )
                                            Text(
                                                row.endTime,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    width = timeWidth
                                )
                                
                                // Title/Description
                                TableCell(
                                    content = {
                                        Column(
                                            modifier = Modifier.clickable {
                                                editingRowIndex = index
                                                showEditRowDialog = true
                                            }
                                        ) {
                                            Text(
                                                row.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            if (row.description.isNotBlank()) {
                                                Text(
                                                    row.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    width = titleWidth
                                )
                            }
                            
                            // Scrollable date checkboxes (synchronized with header)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                repeat(daysInMonth) { day ->
                                    val dayNum = day + 1
                                    val isCompleted = row.completedDates.contains(dayNum)
                                    val isToday = dayNum == LocalDate.now().dayOfMonth && currentMonth == YearMonth.now()
                                    
                                    TableCell(
                                        content = {
                                            Checkbox(
                                                checked = isCompleted,
                                                onCheckedChange = { checked ->
                                                    val updatedRows = scheduleRows.toMutableList()
                                                    val updatedRow = updatedRows[index]
                                                    if (checked) {
                                                        updatedRow.completedDates.add(dayNum)
                                                    } else {
                                                        updatedRow.completedDates.remove(dayNum)
                                                    }
                                                    scheduleRows = updatedRows
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        width = dateWidth,
                                        isToday = isToday,
                                        isCompleted = isCompleted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add Row Dialog
    if (showAddRowDialog) {
        AddScheduleRowDialog(
            existingRows = scheduleRows,
            onDismiss = { showAddRowDialog = false },
            onAdd = { startTime, endTime, title, description ->
                scheduleRows = scheduleRows + ScheduleRow(
                    startTime = startTime,
                    endTime = endTime,
                    title = title,
                    description = description
                )
                showAddRowDialog = false
            }
        )
    }
    
    // Edit Row Dialog
    if (showEditRowDialog && editingRowIndex >= 0) {
        val rowToEdit = scheduleRows[editingRowIndex]
        EditScheduleRowDialog(
            startTime = rowToEdit.startTime,
            endTime = rowToEdit.endTime,
            title = rowToEdit.title,
            description = rowToEdit.description,
            onDismiss = {
                showEditRowDialog = false
                editingRowIndex = -1
            },
            onSave = { startTime, endTime, title, description ->
                val updatedRows = scheduleRows.toMutableList()
                updatedRows[editingRowIndex] = rowToEdit.copy(
                    startTime = startTime,
                    endTime = endTime,
                    title = title,
                    description = description
                )
                scheduleRows = updatedRows
                showEditRowDialog = false
                editingRowIndex = -1
            },
            onDelete = {
                scheduleRows = scheduleRows.filterIndexed { index, _ -> index != editingRowIndex }
                showEditRowDialog = false
                editingRowIndex = -1
            }
        )
    }
}

@Composable
fun TableHeaderCell(text: String, width: Dp, isToday: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .height(50.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TableCell(
    content: @Composable () -> Unit,
    width: Dp,
    isToday: Boolean = false,
    isCompleted: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(70.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(
                when {
                    isCompleted -> Color(0xFFE8F5E9)
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Dialog to add a new schedule row with time pattern suggestion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleRowDialog(
    existingRows: List<Any>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showPatternSuggestion by remember { mutableStateOf(false) }
    var suggestedStartTime by remember { mutableStateOf("") }
    var suggestedEndTime by remember { mutableStateOf("") }
    
    // Calculate pattern suggestion
    LaunchedEffect(existingRows) {
        if (existingRows.isNotEmpty()) {
            showPatternSuggestion = true
            suggestedStartTime = "10:00"
            suggestedEndTime = "12:00"
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Schedule Row", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showPatternSuggestion) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Pattern Suggestion",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "$suggestedStartTime - $suggestedEndTime",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = {
                                startTime = suggestedStartTime
                                endTime = suggestedEndTime
                            }) {
                                Text("Use")
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (HH:MM)") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (HH:MM)") },
                    placeholder = { Text("10:00") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Morning Workout") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Exercise and stretching") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startTime.isNotBlank() && endTime.isNotBlank() && title.isNotBlank()) {
                        onAdd(startTime, endTime, title, description)
                    }
                },
                enabled = startTime.isNotBlank() && endTime.isNotBlank() && title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to edit an existing schedule row
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleRowDialog(
    startTime: String,
    endTime: String,
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onDelete: () -> Unit
) {
    var editStartTime by remember { mutableStateOf(startTime) }
    var editEndTime by remember { mutableStateOf(endTime) }
    var editTitle by remember { mutableStateOf(title) }
    var editDescription by remember { mutableStateOf(description) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Schedule Row", fontWeight = FontWeight.Bold)
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editStartTime,
                    onValueChange = { editStartTime = it },
                    label = { Text("Start Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editEndTime,
                    onValueChange = { editEndTime = it },
                    label = { Text("End Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = editDescription,
                    onValueChange = { editDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editStartTime.isNotBlank() && editEndTime.isNotBlank() && editTitle.isNotBlank()) {
                        onSave(editStartTime, editEndTime, editTitle, editDescription)
                    }
                },
                enabled = editStartTime.isNotBlank() && editEndTime.isNotBlank() && editTitle.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Row?") },
            text = { Text("Are you sure you want to delete this schedule row?") },
            confirmButton = {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
