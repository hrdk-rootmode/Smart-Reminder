package com.groupflow.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.groupflow.app.service.FirebaseAuthService
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.data.local.entity.ReminderPriority
import com.groupflow.app.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToGroups: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GroupFlow") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Welcome back!",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "User Name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToGroups,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Groups")
                    }
                    Button(
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tasks")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToReminders,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reminders")
                    }
                    Button(
                        onClick = onNavigateToFiles,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Files")
                    }
                }
            }

            item {
                Text(
                    text = "Recent Groups",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToGroups
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Project Team",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "5 members • 3 tasks pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToGroups
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Family Group",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "8 members • 2 reminders today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Upcoming Tasks",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToTasks
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Complete project proposal",
                                style = MaterialTheme.typography.titleSmall
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("High Priority") },
                                leadingIcon = {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                }
                            )
                        }
                        Text(
                            text = "Due: Tomorrow, 5:00 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToTasks
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Review meeting notes",
                                style = MaterialTheme.typography.titleSmall
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Medium Priority") },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                }
                            )
                        }
                        Text(
                            text = "Due: Friday, 2:00 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Today's Reminders",
                    style = MaterialTheme.typography.titleMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToReminders
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Call mom at 6 PM",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                        Text(
                            text = "6:00 PM • Recurring weekly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onGroupClick: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Groups") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Add, contentDescription = "Join Group")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search groups...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
            }
            
            item {
                Text(
                    text = "Your Groups",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Project Team", "5 members", "Active 2h ago"),
                Triple("Family Group", "8 members", "Active 1d ago"),
                Triple("Study Group", "4 members", "Active 3d ago"),
                Triple("Work Team", "12 members", "Active 5d ago")
            )) { (name, members, activity) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onGroupClick(name.lowercase().replace(" ", "_")) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = members,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = activity,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Invited Groups",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Neighborhood Watch", "15 members", "Invited 2d ago"),
                Triple("Book Club", "6 members", "Invited 1w ago")
            )) { (name, members, activity) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = members,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = activity,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { }) {
                            Text("Join")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: ReminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAddReminder: () -> Unit = {},
    onSignIn: () -> Unit = {},
    isGuest: Boolean = true
) {
    val reminders by viewModel.getUserReminders().collectAsState(initial = emptyList<Reminder>())
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("guest_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Track banner dismissals and app opens
    val appOpenCount by remember { mutableStateOf(sharedPreferences.getInt("app_open_count", 0)) }
    val bannerDismissed by remember { mutableStateOf(sharedPreferences.getBoolean("banner_dismissed", false)) }
    var showBanner by remember { mutableStateOf(!bannerDismissed || appOpenCount % 3 == 0) }
    
    // Voice input state for logged-in users
    var voiceInput by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    // Increment app open count
    LaunchedEffect(Unit) {
        sharedPreferences.edit()
            .putInt("app_open_count", appOpenCount + 1)
            .apply()
    }
    
    // Calculate progress
    val completedCount = reminders.count { it.status.name == "COMPLETED" }
    val totalCount = reminders.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Progress tracking display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Progress",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$completedCount/$totalCount completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Voice input section for logged-in users
        if (!isGuest) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, top = 8.dp, bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🎤 Tell me your reminder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Large microphone button
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                // TODO: Implement voice recognition
                                isListening = !isListening
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Phone else Icons.Default.Add,
                            contentDescription = "Voice Input",
                            modifier = Modifier.size(56.dp),
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = if (isListening) "Listening..." else "Tap to speak",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "or type below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Manual input fallback
                    OutlinedTextField(
                        value = voiceInput,
                        onValueChange = { voiceInput = it },
                        label = { Text("Or type your reminder here") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                        singleLine = true
                    )
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reminders.isNullOrEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Spacer(modifier = Modifier.height(80.dp))
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No reminders yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add your first reminder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Group reminders by date
                val groupedReminders: Map<String, List<Reminder>> = reminders.groupBy { reminder ->
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = reminder.triggerTime
                    val today = java.util.Calendar.getInstance()
                    val tomorrow = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH, 1) }
                    
                    when {
                        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"
                        calendar.get(java.util.Calendar.YEAR) == tomorrow.get(java.util.Calendar.YEAR) &&
                        calendar.get(java.util.Calendar.DAY_OF_YEAR) == tomorrow.get(java.util.Calendar.DAY_OF_YEAR) -> "Tomorrow"
                        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                        calendar.get(java.util.Calendar.WEEK_OF_YEAR) == today.get(java.util.Calendar.WEEK_OF_YEAR) -> "This Week"
                        else -> "Later"
                    }
                }
                
                groupedReminders.forEach { (dateGroup, dateReminders) ->
                    item {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(dateReminders) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onComplete = { viewModel.markAsCompleted(reminder.reminderId) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onSnooze = { viewModel.snoozeReminder(reminder.reminderId, System.currentTimeMillis() + 15 * 60 * 1000) }
                        )
                    }
                }
            }
        }
        
        // Sign in banner for guest users
        if (isGuest && showBanner) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, top = 8.dp, bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🚀",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column {
                            Text(
                                text = "Sign in to use AI voice reminders",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Unlock voice input, multi-language support & more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {
                            showBanner = false
                            sharedPreferences.edit()
                                .putBoolean("banner_dismissed", true)
                                .apply()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                        Button(
                            onClick = onSignIn,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Sign In", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun TaskCard(title: String, priority: String, dueDate: String, assignee: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = {},
                    label = { Text(priority) },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = assignee,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Call mom at 6 PM", "6:00 PM", "Recurring weekly"),
                Triple("Take medication", "8:00 AM", "Daily"),
                Triple("Team standup", "10:00 AM", "Weekdays")
            )) { (title, time, recurring) ->
                ReminderCard(title = title, time = time, recurring = recurring)
            }
            
            item {
                Text(
                    text = "Tomorrow",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Doctor appointment", "2:00 PM", "One-time"),
                Triple("Pay electricity bill", "5:00 PM", "Monthly")
            )) { (title, time, recurring) ->
                ReminderCard(title = title, time = time, recurring = recurring)
            }
            
            item {
                Text(
                    text = "Upcoming",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Birthday party", "Saturday, 7:00 PM", "Yearly"),
                Triple("Car service", "Monday, 9:00 AM", "One-time")
            )) { (title, time, recurring) ->
                ReminderCard(title = title, time = time, recurring = recurring)
            }
        }
    }
}

@Composable
fun ReminderCard(title: String, time: String, recurring: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (recurring != "One-time") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = recurring,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Complete")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Snooze")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    groupId: String,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Project Team")
                        Text("5 members", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Call, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                items(listOf(
                    Quintuple("John", "Hey everyone, how's the project going?", "10:30 AM", true, false),
                    Quintuple("You", "Going well! I'll have the proposal ready by tomorrow.", "10:32 AM", false, true),
                    Quintuple("Sarah", "Great! I'll review it once you're done.", "10:35 AM", true, false),
                    Quintuple("Mike", "Don't forget to include the budget estimates.", "10:40 AM", true, false),
                    Quintuple("You", "Got it! Adding the budget section now.", "10:42 AM", false, true)
                )) { (sender, message, time, isOther, isMe) ->
                    MessageBubble(
                        sender = sender,
                        message = message,
                        time = time,
                        isOther = isOther,
                        isMe = isMe
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Attach")
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Recent Conversations",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(listOf(
                Triple("Project Team", "Hey everyone, how's the project going?", "10:30 AM"),
                Triple("Family Group", "Don't forget the dinner at 7 PM", "9:15 AM"),
                Triple("Study Group", "Meeting notes are shared", "Yesterday"),
                Triple("Work Team", "Deadline extended to Friday", "Yesterday")
            )) { (name, lastMessage, time) ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class Quintuple<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

@Composable
fun MessageBubble(sender: String, message: String, time: String, isOther: Boolean, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (isOther) {
            Text(
                text = sender,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen() {
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Files") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Create, contentDescription = "New Folder")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Create, contentDescription = "Upload")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search files...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
            }
            
            item {
                Text(
                    text = "Folders",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Pair("Project Documents", "12 files"),
                Pair("Shared Photos", "48 files"),
                Pair("Meeting Notes", "8 files"),
                Pair("Resources", "23 files")
            )) { (name, count) ->
                FolderCard(name = name, count = count)
            }
            
            item {
                Text(
                    text = "Recent Files",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(listOf(
                Triple("Project Proposal.pdf", "2.4 MB", "PDF"),
                Triple("Budget Spreadsheet.xlsx", "856 KB", "Excel"),
                Triple("Presentation.pptx", "5.1 MB", "PowerPoint"),
                Triple("Meeting Notes.docx", "124 KB", "Word"),
                Triple("Design Assets.zip", "45.2 MB", "ZIP")
            )) { (name, size, type) ->
                FileCard(name = name, size = size, type = type)
            }
        }
    }
}

@Composable
fun FolderCard(name: String, count: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Create,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun FileCard(name: String, size: String, type: String) {
    val icon = when (type) {
        "PDF" -> Icons.Default.Star
        "Excel" -> Icons.Default.Star
        "PowerPoint" -> Icons.Default.PlayArrow
        "Word" -> Icons.Default.Star
        "ZIP" -> Icons.Default.Star
        else -> Icons.Default.Star
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$size • $type",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    firebaseAuthService: FirebaseAuthService,
    onLogout: () -> Unit,
    onSignIn: () -> Unit = {}
) {
    val currentUser by firebaseAuthService.currentUser.collectAsState(initial = null)
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("reminder_prefs", android.content.Context.MODE_PRIVATE) }
    var alarmSoundEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("alarm_sound_enabled", true)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = currentUser?.displayName ?: "Guest User",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = currentUser?.email ?: "guest@local",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Profile")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Default.Email,
                            title = "Email",
                            subtitle = currentUser?.email ?: "guest@local",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Create,
                            title = "Sync Settings",
                            onClick = {}
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Enabled",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Settings,
                            title = "Dark Mode",
                            subtitle = "System default",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Person,
                            title = "Language",
                            subtitle = "English",
                            onClick = {}
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Alarm Sound",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Play sound for reminder notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            var alarmSoundEnabled by remember { 
                                mutableStateOf(
                                    sharedPreferences.getBoolean("alarm_sound_enabled", true)
                                ) 
                            }
                            Switch(
                                checked = alarmSoundEnabled,
                                onCheckedChange = { enabled ->
                                    alarmSoundEnabled = enabled
                                    sharedPreferences.edit()
                                        .putBoolean("alarm_sound_enabled", enabled)
                                        .apply()
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Storage",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Storage Used",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "0 MB / 15 GB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "0%",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = 0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            subtitle = "1.0.0",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Info,
                            title = "Help & Support",
                            onClick = {}
                        )
                        HorizontalDivider()
                        SettingItem(
                            icon = Icons.Default.Star,
                            title = "Privacy Policy",
                            onClick = {}
                        )
                    }
                }
            }
            
            item {
                if (currentUser != null) {
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sign Out")
                    }
                } else {
                    Button(
                        onClick = onSignIn,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onSnooze: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (reminder.priority) {
                ReminderPriority.URGENT -> MaterialTheme.colorScheme.errorContainer
                ReminderPriority.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                ReminderPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                ReminderPriority.LOW -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.status.name == "COMPLETED",
                onCheckedChange = { if (it) onComplete() }
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (reminder.status.name == "COMPLETED") androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatDateTime(reminder.triggerTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Snooze") },
                        onClick = {
                            onSnooze()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String, triggerTime: Long, priority: ReminderPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Initialize with current time
    val calendar = Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var isAM by remember { mutableStateOf(calendar.get(Calendar.AM_PM) == Calendar.AM) }
    var selectedPriority by remember { mutableStateOf(ReminderPriority.MEDIUM) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Add Reminder",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 2
                )
                
                // Time Display - Click to open time picker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')} ${if (isAM) "AM" else "PM"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Priority - Radio buttons in 2x2 grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Priority", style = MaterialTheme.typography.bodyMedium)
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderPriority.values().slice(0..1).forEach { priority ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPriority = priority },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedPriority == priority,
                                        onClick = { selectedPriority = priority }
                                    )
                                    Text(
                                        priority.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderPriority.values().slice(2..3).forEach { priority ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPriority = priority },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedPriority == priority,
                                        onClick = { selectedPriority = priority }
                                    )
                                    Text(
                                        priority.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val calendar = Calendar.getInstance()
                        val hour24 = if (isAM) {
                            if (selectedHour == 12) 0 else selectedHour
                        } else {
                            if (selectedHour == 12) 12 else selectedHour + 12
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, hour24)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        calendar.set(Calendar.SECOND, 0)
                        
                        if (calendar.timeInMillis < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                        }
                        
                        onAdd(title, description, calendar.timeInMillis, selectedPriority)
                    }
                },
                enabled = title.isNotBlank()
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
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hour Slider
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Hour: $selectedHour")
                        Slider(
                            value = selectedHour.toFloat(),
                            onValueChange = { selectedHour = it.toInt() },
                            valueRange = if (isAM) 1f..11f else 1f..12f,
                            steps = if (isAM) 10 else 11,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Minute Slider
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Minute: ${selectedMinute.toString().padStart(2, '0')}")
                        Slider(
                            value = selectedMinute.toFloat(),
                            onValueChange = { selectedMinute = it.toInt() },
                            valueRange = 0f..59f,
                            steps = 59,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // AM/PM Toggle
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isAM = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("AM")
                        }
                        Button(
                            onClick = { isAM = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text("PM")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTimePicker = false }) {
                    Text("Done")
                }
            }
        )
    }
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
