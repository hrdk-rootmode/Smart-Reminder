package com.groupflow.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.groupflow.app.service.FirebaseAuthService
import com.groupflow.app.service.SpeechRecognitionHelper
import com.groupflow.app.service.GeminiAIService
import com.groupflow.app.data.local.entity.Reminder
import com.groupflow.app.data.local.entity.ReminderPriority
import com.groupflow.app.notification.NotificationHelper
import com.groupflow.app.ui.viewmodel.ReminderViewModel
import com.groupflow.app.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.text.format.DateFormat
import kotlinx.coroutines.launch

private data class LocalParsedInput(
    val title: String,
    val triggerTime: Long
)

private fun containsDevanagari(text: String): Boolean {
    return text.any { it in '\u0900'..'\u097F' }
}

private fun parseExplicitClockTime(input: String): LocalParsedInput? {
    val trimmed = input.trim()

    // English time patterns: "2:00 PM", "2 PM", "14:00"
    val en12h = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b", RegexOption.IGNORE_CASE)
    val en24h = Regex("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b")

    // Hindi time patterns: "2 बजे", "2:15 बजे", also allow am/pm if user says it
    val hiBaje = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*बजे\\b")
    val hiAmPm = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b", RegexOption.IGNORE_CASE)

    val match12h = en12h.find(trimmed) ?: hiAmPm.find(trimmed)
    val match24h = en24h.find(trimmed)
    val matchBaje = hiBaje.find(trimmed)

    val (hour, minute, isPm, matchedRange) = when {
        match12h != null -> {
            val h = match12h.groupValues[1].toIntOrNull() ?: return null
            val m = match12h.groupValues[2].toIntOrNull() ?: 0
            val ampm = match12h.groupValues[3].lowercase(Locale.getDefault())
            val pm = ampm == "pm"
            Quad(h, m, pm, match12h.range)
        }
        matchBaje != null -> {
            val h = matchBaje.groupValues[1].toIntOrNull() ?: return null
            val m = matchBaje.groupValues[2].toIntOrNull() ?: 0
            // Hindi "बजे" is ambiguous; default to next occurrence using 24h guess:
            // if hour is 1..11, keep as-is; if 12 keep 12.
            Quad(h, m, null, matchBaje.range)
        }
        match24h != null -> {
            val h = match24h.groupValues[1].toIntOrNull() ?: return null
            val m = match24h.groupValues[2].toIntOrNull() ?: return null
            Quad(h, m, null, match24h.range)
        }
        else -> return null
    }

    val calendar = Calendar.getInstance()
    val hour24 = when (isPm) {
        true -> {
            when (hour) {
                12 -> 12
                in 1..11 -> hour + 12
                else -> hour
            }
        }
        false -> {
            when (hour) {
                12 -> 0
                else -> hour
            }
        }
        null -> hour
    }

    calendar.set(Calendar.HOUR_OF_DAY, hour24.coerceIn(0, 23))
    calendar.set(Calendar.MINUTE, minute.coerceIn(0, 59))
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Title: remove common connectors + remove time portion
    var title = trimmed
    title = title.replaceRange(matchedRange, "").trim()
    title = title.replace(Regex("\\b(at|@)\\b", RegexOption.IGNORE_CASE), " ")
    title = title.replace(Regex("\\b(ko|पर|pe)\\b", RegexOption.IGNORE_CASE), " ")
    title = title.replace(Regex("\\s+"), " ").trim()

    if (title.isBlank()) return null
    return LocalParsedInput(title = title, triggerTime = calendar.timeInMillis)
}

// Helper to carry multiple values from when-expression without extra dependencies
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun to24Hour(hour12: Int, isAM: Boolean): Int {
    return if (isAM) {
        if (hour12 == 12) 0 else hour12
    } else {
        if (hour12 == 12) 12 else hour12 + 12
    }
}

private fun to12Hour(hour24: Int): Pair<Int, Boolean> {
    val normalized = ((hour24 % 24) + 24) % 24
    val am = normalized < 12
    val hour12 = when (normalized % 12) {
        0 -> 12
        else -> normalized % 12
    }
    return hour12 to am
}

@Composable
private fun WheelPickerColumn(
    modifier: Modifier,
    items: List<String>,
    initialIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val safeInitialIndex = initialIndex.coerceIn(0, items.lastIndex)
    val virtualCount = Int.MAX_VALUE
    val midpoint = virtualCount / 2
    val baseIndex = midpoint - (midpoint % items.size)
    val initialVirtualIndex = baseIndex + safeInitialIndex
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialVirtualIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }
            centeredItem?.let { item ->
                val normalizedCenter = ((item.index % items.size) + items.size) % items.size
                onSelectedIndexChange(normalizedCenter)
            }
        }
    }

    // Also trigger selection change when scrolling stops at any position
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }
            centeredItem?.let { item ->
                val normalizedCenter = ((item.index % items.size) + items.size) % items.size
                onSelectedIndexChange(normalizedCenter)
            }
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(28.dp)
                )
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 56.dp)
        ) {
            items(virtualCount) { index ->
                val normalizedIndex = ((index % items.size) + items.size) % items.size
                val layoutInfo = listState.layoutInfo
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                
                // Find the item closest to center for accurate highlighting
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                val isSelected = itemInfo?.let { item ->
                    val itemCenter = item.offset + item.size / 2
                    val distanceFromCenter = kotlin.math.abs(itemCenter - viewportCenter)
                    distanceFromCenter < item.size / 2
                } ?: false

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[normalizedIndex],
                        style = if (isSelected) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .alpha(
                                when {
                                    isSelected -> 1f
                                    itemInfo?.let { item ->
                                        val itemCenter = item.offset + item.size / 2
                                        val distanceFromCenter = kotlin.math.abs(itemCenter - viewportCenter)
                                        distanceFromCenter < item.size * 1.5
                                    } == true -> 0.65f
                                    else -> 0.2f
                                }
                            )
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

enum class ChatState {
    IDLE,           // Waiting for user to start
    ASKING_APP,     // Asking user to select an app
    ASKING_REMINDER, // Asking "What reminder?"
    ASKING_TIME,    // Asking for time if not provided
    ASKING_PRIORITY,// Asking for priority
    ASKING_SOUND,   // Asking for sound preference
    LISTENING,      // Waiting for user speech input
    PROCESSING      // Processing the reminder
}

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
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
    isGuest: Boolean = true,
    showCalendarDialog: Boolean = false,
    onDismissCalendar: () -> Unit = {},
    onOpenCalendar: () -> Unit = {}, // New callback to open calendar
    showReportsDialog: Boolean = false,
    onDismissReports: () -> Unit = {}
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
    var detectedLanguage by remember { mutableStateOf("en-US") }
    var chatState by remember { mutableStateOf(ChatState.IDLE) }
    var aiQuestion by remember { mutableStateOf("What would you like to be reminded about?") }
    var pendingSpeechResult by remember { mutableStateOf<String?>(null) }
    
    // Conversational state - store user's answers
    var reminderTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    var explicitTriggerTime by remember { mutableStateOf<Long?>(null) }
    var reminderPriority by remember { mutableStateOf("MEDIUM") }
    var reminderSound by remember { mutableStateOf("sound") }
    var selectedApp by remember { mutableStateOf<com.groupflow.app.service.AppInfo?>(null) }
    
    // Chat message history
    data class ChatMessage(val isUser: Boolean, val text: String)
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    
    // App selection state
    var showAppSelector by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf(listOf<com.groupflow.app.service.AppInfo>()) }
    val appAutomationHelper = remember { com.groupflow.app.service.AppAutomationHelper(context) }
    
    // Edit reminder state
    var showEditDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }
    
    // Get MainActivity reference for speech recognition
    val activity = context as? MainActivity
    
    // Gemini AI service for parsing
    val geminiAIService = remember { GeminiAIService(context).apply { initialize() } }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Watch for speech results from MainActivity
    LaunchedEffect(Unit) {
        while (true) {
            val result = activity?.speechResult
            if (result != null) {
                when {
                    result == "ERROR_NOT_AVAILABLE" -> {
                        // Speech recognition not available
                        voiceInput = ""
                        isListening = false
                        chatState = ChatState.IDLE
                        activity.speechResult = null
                        // Show error to user
                        android.widget.Toast.makeText(
                            context,
                            "Voice search is not available on this device",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                    result.isNotEmpty() -> {
                        // Valid speech result - process with AI if logged in
                        voiceInput = result
                        isListening = false
                        activity.speechResult = null
                        
                        // Auto-process voice input with AI for logged-in users
                        if (!isGuest && chatState == ChatState.LISTENING) {
                            coroutineScope.launch {
                                // Use Gemini AI to parse the voice input
                                val parseResult = geminiAIService.parseReminder(result, viewModel.currentUserId.value)
                                parseResult.onSuccess { parsedReminder ->
                                    // Add user message
                                    chatMessages = chatMessages + ChatMessage(true, result)
                                    
                                    // Set reminder details from AI parsing
                                    reminderTitle = parsedReminder.title
                                    explicitTriggerTime = parsedReminder.triggerTime
                                    reminderPriority = parsedReminder.priority
                                    
                                    // Create reminder directly if all info is available
                                    if (parsedReminder.title.isNotBlank() && parsedReminder.triggerTime > System.currentTimeMillis()) {
                                        viewModel.createReminder(
                                            title = parsedReminder.title,
                                            description = parsedReminder.description,
                                            triggerTime = parsedReminder.triggerTime,
                                            priority = when (parsedReminder.priority) {
                                                "URGENT" -> ReminderPriority.URGENT
                                                "HIGH" -> ReminderPriority.HIGH
                                                "LOW" -> ReminderPriority.LOW
                                                else -> ReminderPriority.MEDIUM
                                            }
                                        )
                                        
                                        // Show success message
                                        val timeStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(parsedReminder.triggerTime))
                                        val successMsg = "✅ Reminder created: \"${parsedReminder.title}\" at $timeStr"
                                        chatMessages = chatMessages + ChatMessage(false, successMsg)
                                        
                                        // Reset state
                                        voiceInput = ""
                                        chatState = ChatState.IDLE
                                        reminderTitle = ""
                                        explicitTriggerTime = null
                                        reminderPriority = "MEDIUM"
                                    } else {
                                        // Ask for missing information
                                        voiceInput = ""
                                        chatState = ChatState.ASKING_TIME
                                        val question = "What time should I remind you?"
                                        aiQuestion = question
                                        chatMessages = chatMessages + ChatMessage(false, question)
                                    }
                                }.onFailure {
                                    // Fallback to manual input
                                    chatState = ChatState.ASKING_REMINDER
                                    aiQuestion = "I couldn't understand that. What would you like to be reminded about?"
                                    chatMessages = chatMessages + ChatMessage(false, aiQuestion)
                                }
                            }
                        } else {
                            // For guests or non-listening state, just set the input
                            chatState = ChatState.ASKING_REMINDER
                        }
                    }
                    else -> {
                        // Empty result (cancelled)
                        isListening = false
                        activity.speechResult = null
                    }
                }
            }
            kotlinx.coroutines.delay(300)
        }
    }
    
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
    
    // Calendar and filter state
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf("All") } // All, Yesterday, Today, Tomorrow, This Week, Later
    
    // Filter reminders: by default show Today and future only (no Yesterday/This Week unless filtered)
    val filteredReminders = remember(reminders, selectedDate, selectedFilter) {
        when {
            selectedDate != null -> {
                val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                reminders.filter { reminder ->
                    val reminderCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    selectedCal.get(Calendar.YEAR) == reminderCal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.DAY_OF_YEAR) == reminderCal.get(Calendar.DAY_OF_YEAR)
                }
            }
            selectedFilter == "Yesterday" -> {
                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                }
            }
            selectedFilter == "Today" -> {
                val today = Calendar.getInstance()
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                }
            }
            selectedFilter == "Tomorrow" -> {
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
                }
            }
            selectedFilter == "This Week" -> {
                val today = Calendar.getInstance()
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
                }
            }
            selectedFilter == "Later" -> {
                val today = Calendar.getInstance()
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    calendar.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
                    (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                     calendar.get(Calendar.WEEK_OF_YEAR) > today.get(Calendar.WEEK_OF_YEAR))
                }
            }
            else -> {
                // Default: Show only Today and future reminders
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                reminders.filter { it.triggerTime >= today.timeInMillis }
            }
        }
    }
    
    // Get dates with reminders for calendar dots
    val datesWithReminders = remember(reminders) {
        reminders.map { reminder ->
            val cal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips - moved to top
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Yesterday", "Today", "Tomorrow", "This Week", "Later")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter && selectedDate == null,
                        onClick = {
                            selectedFilter = filter
                            selectedDate = null // Reset date selection when filter is selected
                        },
                        label = { Text(filter, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = if (selectedFilter == filter && selectedDate == null) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
            
            // Progress tracking display - compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedDate != null) {
                            "Selected Date Progress"
                        } else if (selectedFilter != "All") {
                            "$selectedFilter Progress"
                        } else {
                            "Daily Progress"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${filteredReminders.count { it.status.name == "COMPLETED" }}/${filteredReminders.size} completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { 
                        if (filteredReminders.isNotEmpty()) 
                            filteredReminders.count { it.status.name == "COMPLETED" }.toFloat() / filteredReminders.size 
                        else 0f 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
            }
        
        // Conversational AI chat interface for logged-in users
        if (!isGuest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Chat message history - shows conversation like a real chat
                if (chatMessages.isNotEmpty() || chatState != ChatState.IDLE) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        reverseLayout = false,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show chat messages
                        items(chatMessages) { message ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                shape = RoundedCornerShape(
                                    topStart = if (message.isUser) 20.dp else 4.dp,
                                    topEnd = if (message.isUser) 4.dp else 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                ),
                                color = if (message.isUser) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 1.dp
                            ) {
                                Text(
                                    text = message.text,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (message.isUser)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Show current AI question if not IDLE
                        if (chatState != ChatState.IDLE && chatState != ChatState.PROCESSING) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    shape = RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 20.dp,
                                        bottomStart = 20.dp,
                                        bottomEnd = 20.dp
                                    ),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    tonalElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "AI",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = aiQuestion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Input bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Microphone button
                        FilledIconButton(
                            onClick = {
                                when (chatState) {
                                    ChatState.IDLE -> {
                                        // Check if user wants to open an app
                                        if (voiceInput.lowercase().contains("open app") || 
                                            voiceInput.lowercase().contains("launch app") ||
                                            voiceInput.lowercase().contains("start app")) {
                                            // Show app selector
                                            installedApps = appAutomationHelper.getAllInstalledApps()
                                            showAppSelector = true
                                            chatState = ChatState.ASKING_APP
                                            aiQuestion = "Select an app to open"
                                        } else {
                                            // Start normal reminder conversation
                                            chatMessages = emptyList()
                                            chatState = ChatState.ASKING_REMINDER
                                            aiQuestion = "What would you like to be reminded about?"
                                            chatMessages = chatMessages + ChatMessage(false, aiQuestion)
                                        }
                                    }
                                    ChatState.LISTENING -> {
                                        // Stop listening - cancel speech recognition
                                        isListening = false
                                        chatState = ChatState.ASKING_REMINDER
                                    }
                                    else -> {
                                        // Launch speech recognizer
                                        isListening = true
                                        activity?.launchSpeechRecognizer(detectedLanguage)
                                        chatState = ChatState.LISTENING
                                    }
                                }
                            },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = when (chatState) {
                                    ChatState.LISTENING -> MaterialTheme.colorScheme.error
                                    ChatState.ASKING_APP, ChatState.ASKING_REMINDER, ChatState.ASKING_TIME, ChatState.ASKING_PRIORITY, ChatState.ASKING_SOUND -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Icon(
                                imageVector = when (chatState) {
                                    ChatState.LISTENING -> Icons.Default.Close
                                    else -> Icons.Default.Search
                                },
                                contentDescription = "Speak",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                        
                        // Text input field
                        OutlinedTextField(
                            value = voiceInput,
                            onValueChange = { voiceInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { 
                                Text(
                                    when (chatState) {
                                        ChatState.LISTENING -> "Listening..."
                                        ChatState.ASKING_APP -> "Type 'open app' to select an app, or your reminder..."
                                        ChatState.ASKING_REMINDER -> "Type or speak your reminder..."
                                        ChatState.ASKING_TIME -> "Type or speak the time..."
                                        ChatState.ASKING_PRIORITY -> "Type or speak priority (low, medium, high, urgent)..."
                                        ChatState.ASKING_SOUND -> "Type or speak sound preference (ring, vibrate, silent)..."
                                        ChatState.PROCESSING -> "Creating reminder..."
                                        else -> "Type 'open app' or your reminder..."
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                ) 
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodySmall,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (voiceInput.isNotBlank()) {
                                        coroutineScope.launch {
                                            processConversationalInput(
                                                input = voiceInput,
                                                chatState = chatState,
                                                geminiAIService = geminiAIService,
                                                viewModel = viewModel,
                                                coroutineScope = coroutineScope,
                                                onStateChange = { chatState = it },
                                                onQuestionChange = { aiQuestion = it },
                                                onClearInput = { voiceInput = ""; pendingSpeechResult = null },
                                                onAddMessage = { isUser, text -> chatMessages = chatMessages + ChatMessage(isUser, text) },
                                                reminderTitle = reminderTitle,
                                                onReminderTitleChange = { reminderTitle = it },
                                                reminderTime = reminderTime,
                                                onReminderTimeChange = { reminderTime = it },
                                                explicitTriggerTime = explicitTriggerTime,
                                                onExplicitTriggerTimeChange = { explicitTriggerTime = it },
                                                reminderPriority = reminderPriority,
                                                onReminderPriorityChange = { reminderPriority = it },
                                                reminderSound = reminderSound,
                                                onReminderSoundChange = { reminderSound = it },
                                                selectedApp = selectedApp,
                                                onSelectedAppChange = { selectedApp = it },
                                                appAutomationHelper = appAutomationHelper,
                                                onShowAppSelector = { showAppSelector = it },
                                                onInstalledAppsChange = { installedApps = it }
                                            )
                                        }
                                    }
                                }
                            )
                        )
                        
                        // Send button
                        FilledIconButton(
                            onClick = {
                                if (voiceInput.isNotBlank()) {
                                    coroutineScope.launch {
                                        processConversationalInput(
                                            input = voiceInput,
                                            chatState = chatState,
                                            geminiAIService = geminiAIService,
                                            viewModel = viewModel,
                                            coroutineScope = coroutineScope,
                                            onStateChange = { chatState = it },
                                            onQuestionChange = { aiQuestion = it },
                                            onClearInput = { voiceInput = ""; pendingSpeechResult = null },
                                            onAddMessage = { isUser, text -> chatMessages = chatMessages + ChatMessage(isUser, text) },
                                            reminderTitle = reminderTitle,
                                            onReminderTitleChange = { reminderTitle = it },
                                            reminderTime = reminderTime,
                                            onReminderTimeChange = { reminderTime = it },
                                            explicitTriggerTime = explicitTriggerTime,
                                            onExplicitTriggerTimeChange = { explicitTriggerTime = it },
                                            reminderPriority = reminderPriority,
                                            onReminderPriorityChange = { reminderPriority = it },
                                            reminderSound = reminderSound,
                                            onReminderSoundChange = { reminderSound = it },
                                            selectedApp = selectedApp,
                                            onSelectedAppChange = { selectedApp = it },
                                            appAutomationHelper = appAutomationHelper,
                                            onShowAppSelector = { showAppSelector = it },
                                            onInstalledAppsChange = { installedApps = it }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = voiceInput.isNotBlank() && chatState != ChatState.PROCESSING
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // App Selector Dialog
        if (showAppSelector) {
            AlertDialog(
                onDismissRequest = { showAppSelector = false },
                title = { Text("Select an App") },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(installedApps) { app ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                onClick = {
                                    selectedApp = app
                                    showAppSelector = false
                                    // Continue with time question
                                    aiQuestion = "What time should I open ${app.appName}?"
                                    chatMessages = chatMessages + ChatMessage(false, aiQuestion)
                                    chatState = ChatState.ASKING_TIME
                                    voiceInput = ""
                                    pendingSpeechResult = null
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Display app icon using Image
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = (app.icon as android.graphics.drawable.BitmapDrawable).bitmap.asImageBitmap(),
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = app.appName.firstOrNull()?.toString() ?: "?",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAppSelector = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Define background colors for each date group (outside LazyColumn to avoid Composable context issues)
        // Now using border colors instead of background colors
        val dateGroupBorderColors = mapOf(
            "Yesterday" to MaterialTheme.colorScheme.error,
            "Today" to MaterialTheme.colorScheme.primary,
            "Tomorrow" to MaterialTheme.colorScheme.tertiary,
            "This Week" to MaterialTheme.colorScheme.secondary,
            "Later" to MaterialTheme.colorScheme.outline
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredReminders.isNullOrEmpty()) {
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
                            text = if (selectedDate != null || selectedFilter != "All") "No reminders for this filter" else "No reminders yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedDate != null || selectedFilter != "All") "Try a different filter" else "Tap the + button to add your first reminder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Group reminders by date
                val groupedReminders: Map<String, List<Reminder>> = filteredReminders.groupBy { reminder ->
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = reminder.triggerTime
                    val today = java.util.Calendar.getInstance()
                    val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH, -1) }
                    val tomorrow = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH, 1) }
                    
                    when {
                        calendar.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                        calendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR) -> "Yesterday"
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
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    items(dateReminders) { reminder ->
                        // Get border color for this date group
                        val borderColor = dateGroupBorderColors[dateGroup] ?: MaterialTheme.colorScheme.outline
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 2.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            ReminderItem(
                                reminder = reminder,
                                onComplete = { viewModel.markAsCompleted(reminder.reminderId) },
                                onDelete = { viewModel.deleteReminder(reminder) },
                                onSnooze = { viewModel.snoozeReminder(reminder.reminderId, System.currentTimeMillis() + 15 * 60 * 1000) },
                                onEdit = { 
                                    reminderToEdit = reminder
                                    showEditDialog = true
                                },
                                onUnmarkCompleted = { viewModel.unmarkCompleted(reminder.reminderId) }
                            )
                        }
                    }
                }
                
                // View All button at the bottom
                item {
                    TextButton(
                        onClick = onOpenCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "View All Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    } // Scaffold closes here
        
    // Edit reminder dialog
    if (showEditDialog && reminderToEdit != null) {
            EditReminderDialog(
                reminder = reminderToEdit!!,
                onDismiss = { 
                    showEditDialog = false
                    reminderToEdit = null
                },
                onEdit = { title, description, triggerTime, priority ->
                    viewModel.updateReminder(
                        reminderToEdit!!.copy(
                            title = title,
                            description = description,
                            triggerTime = triggerTime,
                            priority = priority,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                    showEditDialog = false
                    reminderToEdit = null
                }
            )
        }
        
        // Progress Report Dialog (controlled from TopAppBar menu)
        if (showReportsDialog) {
            ProgressReportDialog(
                reminders = reminders,
                onDismiss = onDismissReports
            )
        }
        
        // Calendar View Dialog (controlled from TopAppBar menu)
        if (showCalendarDialog) {
            CalendarViewDialog(
                reminders = reminders,
                onDismiss = onDismissCalendar,
                viewModel = viewModel
            )
        }
        
        // Sign in banner for guest users
        if (isGuest && showBanner) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, top = 8.dp, bottom = 50.dp),
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
    var darkThemeEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("dark_theme_enabled", false)) }
    
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        text = "Theme Mode",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = when {
                                            sharedPreferences.getBoolean("follow_system_theme", true) -> "Follow System"
                                            darkThemeEnabled -> "Dark"
                                            else -> "Light"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            var showThemeDialog by remember { mutableStateOf(false) }
                            IconButton(onClick = { showThemeDialog = true }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Change theme")
                            }
                            
                            if (showThemeDialog) {
                                ThemeSelectionDialog(
                                    currentTheme = when {
                                        sharedPreferences.getBoolean("follow_system_theme", true) -> "system"
                                        darkThemeEnabled -> "dark"
                                        else -> "light"
                                    },
                                    onThemeSelected = { theme ->
                                        when (theme) {
                                            "system" -> {
                                                sharedPreferences.edit()
                                                    .putBoolean("follow_system_theme", true)
                                                    .apply()
                                            }
                                            "light" -> {
                                                sharedPreferences.edit()
                                                    .putBoolean("follow_system_theme", false)
                                                    .putBoolean("dark_theme_enabled", false)
                                                    .apply()
                                                darkThemeEnabled = false
                                            }
                                            "dark" -> {
                                                sharedPreferences.edit()
                                                    .putBoolean("follow_system_theme", false)
                                                    .putBoolean("dark_theme_enabled", true)
                                                    .apply()
                                                darkThemeEnabled = true
                                            }
                                        }
                                        showThemeDialog = false
                                    },
                                    onDismiss = { showThemeDialog = false }
                                )
                            }
                        }
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Alarm Sound Toggle
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
                        
                        HorizontalDivider()
                        
                        // Force Ring for Urgent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Force Ring (Urgent)",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Ring even when phone is silent for urgent reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            var forceRingUrgent by remember { 
                                mutableStateOf(
                                    sharedPreferences.getBoolean("force_ring_urgent", true)
                                ) 
                            }
                            Switch(
                                checked = forceRingUrgent,
                                onCheckedChange = { enabled ->
                                    forceRingUrgent = enabled
                                    sharedPreferences.edit()
                                        .putBoolean("force_ring_urgent", enabled)
                                        .apply()
                                }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        // Sound Selection
                        var showSoundDialog by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSoundDialog = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Notification Sound",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (sharedPreferences.getBoolean("use_system_default_sound", true)) 
                                        "System Default" else "Custom Sound",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Change sound",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (showSoundDialog) {
                            SoundSelectionDialog(
                                currentUseSystemDefault = sharedPreferences.getBoolean("use_system_default_sound", true),
                                onSoundSelected = { useSystemDefault, customUri ->
                                    sharedPreferences.edit()
                                        .putBoolean("use_system_default_sound", useSystemDefault)
                                        .putString("custom_sound_uri", customUri)
                                        .apply()
                                    showSoundDialog = false
                                    // Recreate notification channels with new sound
                                    NotificationHelper.createNotificationChannel(context)
                                },
                                onDismiss = { showSoundDialog = false }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        // Test Notification Button
                        Button(
                            onClick = {
                                // Create a test reminder 5 seconds from now
                                val testTime = System.currentTimeMillis() + 5000
                                NotificationHelper.scheduleReminder(
                                    context = context,
                                    reminderId = "test_${System.currentTimeMillis()}",
                                    title = "Test Reminder",
                                    description = "This is a test notification to verify your settings",
                                    priority = "URGENT",
                                    triggerTime = testTime
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Notification (5 seconds)")
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
    onSnooze: () -> Unit,
    onEdit: () -> Unit = {},
    onUnmarkCompleted: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (reminder.status.name == "COMPLETED") 0.5f else 1f),
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
                onCheckedChange = { if (it) onComplete() else onUnmarkCompleted() },
                enabled = true
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
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
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
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    // Initialize with current time
    val now = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(now.timeInMillis) }
    var selectedHour by remember {
        mutableStateOf(
            now.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        )
    }
    var selectedMinute by remember { mutableStateOf(now.get(Calendar.MINUTE)) }
    var isAM by remember { mutableStateOf(now.get(Calendar.AM_PM) == Calendar.AM) }
    var is24HourFormat by remember { mutableStateOf(DateFormat.is24HourFormat(context)) }
    var selectedPriority by remember { mutableStateOf(ReminderPriority.MEDIUM) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Listen for speech results
    LaunchedEffect(Unit) {
        while (true) {
            val mainActivity = activity as? com.groupflow.app.MainActivity
            val result = mainActivity?.speechResult
            if (result != null && result.toString().isNotBlank()) {
                title = result.toString()
                mainActivity.speechResult = null
            }
            kotlinx.coroutines.delay(300)
        }
    }
    
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
                
                // Voice Input Button - Siri-like
                var voiceButtonEnabled by remember { mutableStateOf(true) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListening) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                    onClick = {
                        if (voiceButtonEnabled) {
                            if (isListening) {
                                // Stop listening
                                isListening = false
                            } else {
                                // Start listening
                                isListening = true
                                voiceButtonEnabled = false
                                (activity as? com.groupflow.app.MainActivity)?.launchSpeechRecognizer("en-US")
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(5000)
                                    isListening = false
                                    voiceButtonEnabled = true
                                }
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Close else Icons.Default.Settings,
                            contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                            tint = if (isListening) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "Tap to stop listening" else "Tap to speak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isListening) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
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
                            if (is24HourFormat) {
                                val hour24 = to24Hour(selectedHour, isAM)
                                "${hour24.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                            } else {
                                "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')} ${if (isAM) "AM" else "PM"}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Date Display - Click to open date picker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selectedDateMillis)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val hour24 = if (isAM) {
                            if (selectedHour == 12) 0 else selectedHour
                        } else {
                            if (selectedHour == 12) 12 else selectedHour + 12
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, hour24)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        // FIX: Respect user's selected date - don't auto-move to next day
                        // If user selects today with past time, show reminder immediately or at selected time tomorrow only if explicitly requested
                        // For now, just use the selected date/time as-is
                        
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
                val hours12 = (1..12).map { it.toString().padStart(2, '0') }
                val hours24 = (0..23).map { it.toString().padStart(2, '0') }
                val minutes = (0..59).map { it.toString().padStart(2, '0') }
                val ampm = listOf("AM", "PM")
                val initialHour24 = remember(selectedHour, isAM) { to24Hour(selectedHour, isAM) }
                var hourIndex by remember { mutableStateOf(if (is24HourFormat) initialHour24 else (selectedHour.coerceIn(1, 12) - 1)) }
                var minuteIndex by remember { mutableStateOf(selectedMinute.coerceIn(0, 59)) }
                var ampmIndex by remember { mutableStateOf(if (isAM) 0 else 1) }

                LaunchedEffect(is24HourFormat) {
                    if (is24HourFormat) {
                        hourIndex = to24Hour(selectedHour, isAM).coerceIn(0, 23)
                    } else {
                        val (hour12, am) = to12Hour(hourIndex)
                        hourIndex = (hour12 - 1).coerceIn(0, 11)
                        ampmIndex = if (am) 0 else 1
                    }
                }

                LaunchedEffect(hourIndex, is24HourFormat) {
                    if (is24HourFormat) {
                        val (hour12, am) = to12Hour(hourIndex)
                        selectedHour = hour12
                        isAM = am
                        ampmIndex = if (am) 0 else 1
                    } else {
                        selectedHour = (hourIndex + 1).coerceIn(1, 12)
                    }
                }
                LaunchedEffect(minuteIndex) { selectedMinute = minuteIndex }
                LaunchedEffect(ampmIndex, is24HourFormat) {
                    if (!is24HourFormat) {
                        isAM = ampmIndex == 0
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (is24HourFormat) {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = hours24,
                            initialIndex = hourIndex.coerceIn(0, 23),
                            onSelectedIndexChange = { hourIndex = it }
                        )
                    } else {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = hours12,
                            initialIndex = hourIndex.coerceIn(0, 11),
                            onSelectedIndexChange = { hourIndex = it }
                        )
                    }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    WheelPickerColumn(
                        modifier = Modifier
                            .weight(1f)
                            .height(168.dp),
                        items = minutes,
                        initialIndex = minuteIndex,
                        onSelectedIndexChange = { minuteIndex = it }
                    )
                    if (!is24HourFormat) {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = ampm,
                            initialIndex = ampmIndex,
                            onSelectedIndexChange = { ampmIndex = it.coerceIn(0, 1) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTimePicker = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { is24HourFormat = !is24HourFormat }) {
                    Text(if (is24HourFormat) "Use 12-hour" else "Use 24-hour")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onEdit: (title: String, description: String, triggerTime: Long, priority: ReminderPriority) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }
    
    // Initialize with reminder's time
    val initialCalendar = remember { Calendar.getInstance().apply { timeInMillis = reminder.triggerTime } }
    var selectedDateMillis by remember { mutableStateOf(initialCalendar.timeInMillis) }
    var selectedHour by remember {
        mutableStateOf(
            initialCalendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        )
    }
    var selectedMinute by remember { mutableStateOf(initialCalendar.get(Calendar.MINUTE)) }
    var isAM by remember { mutableStateOf(initialCalendar.get(Calendar.AM_PM) == Calendar.AM) }
    var is24HourFormat by remember { mutableStateOf(DateFormat.is24HourFormat(context)) }
    var selectedPriority by remember { mutableStateOf(reminder.priority) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Reminder",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                            if (is24HourFormat) {
                                val hour24 = to24Hour(selectedHour, isAM)
                                "${hour24.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                            } else {
                                "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')} ${if (isAM) "AM" else "PM"}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Date Display - Click to open date picker (ADDED)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Date", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selectedDateMillis)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Priority - Radio buttons in 2x2 grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val hour24 = if (isAM) {
                            if (selectedHour == 12) 0 else selectedHour
                        } else {
                            if (selectedHour == 12) 12 else selectedHour + 12
                        }
                        calendar.set(Calendar.HOUR_OF_DAY, hour24)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        // FIX: Respect user's selected date - don't auto-move to next day
                        
                        onEdit(title, description, calendar.timeInMillis, selectedPriority)
                    }
                },
                enabled = title.isNotBlank()
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
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                val hours12 = (1..12).map { it.toString().padStart(2, '0') }
                val hours24 = (0..23).map { it.toString().padStart(2, '0') }
                val minutes = (0..59).map { it.toString().padStart(2, '0') }
                val ampm = listOf("AM", "PM")
                val initialHour24 = remember(selectedHour, isAM) { to24Hour(selectedHour, isAM) }
                var hourIndex by remember { mutableStateOf(if (is24HourFormat) initialHour24 else (selectedHour.coerceIn(1, 12) - 1)) }
                var minuteIndex by remember { mutableStateOf(selectedMinute.coerceIn(0, 59)) }
                var ampmIndex by remember { mutableStateOf(if (isAM) 0 else 1) }

                LaunchedEffect(is24HourFormat) {
                    if (is24HourFormat) {
                        hourIndex = to24Hour(selectedHour, isAM).coerceIn(0, 23)
                    } else {
                        val (hour12, am) = to12Hour(hourIndex)
                        hourIndex = (hour12 - 1).coerceIn(0, 11)
                        ampmIndex = if (am) 0 else 1
                    }
                }
                LaunchedEffect(hourIndex, is24HourFormat) {
                    if (is24HourFormat) {
                        val (hour12, am) = to12Hour(hourIndex)
                        selectedHour = hour12
                        isAM = am
                        ampmIndex = if (am) 0 else 1
                    } else {
                        selectedHour = hourIndex + 1
                    }
                }
                LaunchedEffect(minuteIndex) { selectedMinute = minuteIndex }
                LaunchedEffect(ampmIndex, is24HourFormat) {
                    if (!is24HourFormat) {
                        isAM = ampmIndex == 0
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (is24HourFormat) {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = hours24,
                            initialIndex = hourIndex.coerceIn(0, 23),
                            onSelectedIndexChange = { hourIndex = it }
                        )
                    } else {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = hours12,
                            initialIndex = hourIndex.coerceIn(0, 11),
                            onSelectedIndexChange = { hourIndex = it }
                        )
                    }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    WheelPickerColumn(
                        modifier = Modifier
                            .weight(1f)
                            .height(168.dp),
                        items = minutes,
                        initialIndex = minuteIndex,
                        onSelectedIndexChange = { minuteIndex = it }
                    )
                    if (!is24HourFormat) {
                        WheelPickerColumn(
                            modifier = Modifier
                                .weight(1f)
                                .height(168.dp),
                            items = ampm,
                            initialIndex = ampmIndex,
                            onSelectedIndexChange = { ampmIndex = it.coerceIn(0, 1) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { is24HourFormat = !is24HourFormat }) {
                    Text(if (is24HourFormat) "Use 12-hour" else "Use 24-hour")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Helper function to process conversational AI input step by step
 */
private suspend fun processConversationalInput(
    input: String,
    chatState: ChatState,
    geminiAIService: GeminiAIService,
    viewModel: ReminderViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStateChange: (ChatState) -> Unit,
    onQuestionChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onAddMessage: (Boolean, String) -> Unit,
    reminderTitle: String,
    onReminderTitleChange: (String) -> Unit,
    reminderTime: String?,
    onReminderTimeChange: (String?) -> Unit,
    explicitTriggerTime: Long?,
    onExplicitTriggerTimeChange: (Long?) -> Unit,
    reminderPriority: String,
    onReminderPriorityChange: (String) -> Unit,
    reminderSound: String,
    onReminderSoundChange: (String) -> Unit,
    selectedApp: com.groupflow.app.service.AppInfo?,
    onSelectedAppChange: (com.groupflow.app.service.AppInfo?) -> Unit,
    appAutomationHelper: com.groupflow.app.service.AppAutomationHelper,
    onShowAppSelector: (Boolean) -> Unit,
    onInstalledAppsChange: (List<com.groupflow.app.service.AppInfo>) -> Unit
) {
    android.util.Log.d("TasksScreen", "Processing input: $input, state: $chatState")
    
    when (chatState) {
        ChatState.IDLE -> {
            // Check if user wants to open an app
            if (input.lowercase().contains("open app") || 
                input.lowercase().contains("launch app") ||
                input.lowercase().contains("start app")) {
                onAddMessage(true, input)
                onClearInput()
                // Show app selector
                val apps = appAutomationHelper.getAllInstalledApps()
                onInstalledAppsChange(apps)
                onShowAppSelector(true)
                onQuestionChange("Select an app to open")
                onStateChange(ChatState.ASKING_APP)
            } else {
                // Normal reminder flow (single-sentence supported)
                onAddMessage(true, input)

                val hasDevanagari = containsDevanagari(input)
                val localParsed = if (hasDevanagari || input.any { it.isDigit() }) {
                    parseExplicitClockTime(input)
                } else {
                    null
                }

                if (localParsed != null) {
                    // English/Hindi explicit clock time -> trust local parse for exact scheduling
                    onReminderTitleChange(localParsed.title)
                    onExplicitTriggerTimeChange(localParsed.triggerTime)
                    onReminderTimeChange("explicit")
                    onClearInput()
                    val question = "What priority? (low, medium, high, urgent)"
                    onQuestionChange(question)
                    onAddMessage(false, question)
                    onStateChange(ChatState.ASKING_PRIORITY)
                } else {
                    // Fallback: Gemini parses (useful for other languages / relative times)
                    val parseResult = geminiAIService.parseReminder(input, viewModel.currentUserId.value)
                    parseResult.onSuccess { parsedReminder ->
                        onReminderTitleChange(parsedReminder.title)
                        // If user mentioned a time, Gemini gives triggerTime. Use it directly.
                        val now = System.currentTimeMillis()
                        if (parsedReminder.triggerTime > now + 60000) {
                            onExplicitTriggerTimeChange(parsedReminder.triggerTime)
                            onReminderTimeChange("ai")
                            onClearInput()
                            val question = "What priority? (low, medium, high, urgent)"
                            onQuestionChange(question)
                            onAddMessage(false, question)
                            onStateChange(ChatState.ASKING_PRIORITY)
                        } else {
                            // No reliable time extracted -> ask user
                            onExplicitTriggerTimeChange(null)
                            onReminderTimeChange(null)
                            onClearInput()
                            val question = "What time should I remind you? (e.g., 'in 5 minutes', 'at 3 PM', 'tomorrow morning')"
                            onQuestionChange(question)
                            onAddMessage(false, question)
                            onStateChange(ChatState.ASKING_TIME)
                        }
                    }.onFailure {
                        onReminderTitleChange(input)
                        onExplicitTriggerTimeChange(null)
                        onReminderTimeChange(null)
                        onClearInput()
                        val question = "What time should I remind you? (e.g., 'in 5 minutes', 'at 3 PM', 'tomorrow morning')"
                        onQuestionChange(question)
                        onAddMessage(false, question)
                        onStateChange(ChatState.ASKING_TIME)
                    }
                }
            }
        }
        
        ChatState.ASKING_APP -> {
            // User typed app name instead of using selector
            onAddMessage(true, input)
            val searchResults = appAutomationHelper.searchApps(input)
            if (searchResults.isNotEmpty()) {
                onSelectedAppChange(searchResults.first())
                val question = "What time should I open ${searchResults.first().appName}?"
                onQuestionChange(question)
                onAddMessage(false, question)
                onStateChange(ChatState.ASKING_TIME)
            } else {
                onQuestionChange("App not found. Try 'open app' to see all apps.")
                onAddMessage(false, "App not found. Try 'open app' to see all apps.")
            }
            onClearInput()
        }
        
        ChatState.ASKING_REMINDER -> {
            // User provided the reminder title
            onAddMessage(true, input)
            onReminderTitleChange(input)
            onClearInput()
            val question = "What time should I remind you? (e.g., 'in 5 minutes', 'at 3 PM', 'tomorrow morning')"
            onQuestionChange(question)
            onAddMessage(false, question)
            onStateChange(ChatState.ASKING_TIME)
        }
        
        ChatState.ASKING_TIME -> {
            // User provided time
            onAddMessage(true, input)

            val localParsed = parseExplicitClockTime("${reminderTitle} $input")
            if (localParsed != null) {
                // If user typed an explicit clock time, keep it exact
                onExplicitTriggerTimeChange(localParsed.triggerTime)
                onReminderTimeChange("explicit")
            } else {
                onReminderTimeChange(input)
                onExplicitTriggerTimeChange(null)
            }

            onClearInput()
            val question = "What priority? (low, medium, high, urgent)"
            onQuestionChange(question)
            onAddMessage(false, question)
            onStateChange(ChatState.ASKING_PRIORITY)
        }
        
        ChatState.ASKING_PRIORITY -> {
            // User provided priority
            onAddMessage(true, input)
            val normalizedPriorityInput = input.lowercase(Locale.getDefault())
            val priority = when {
                normalizedPriorityInput.contains("urgent") || normalizedPriorityInput.contains("immediate") || normalizedPriorityInput == "u" -> "URGENT"
                normalizedPriorityInput.contains("high") || normalizedPriorityInput.contains("important") || normalizedPriorityInput == "h" -> "HIGH"
                normalizedPriorityInput.contains("low") || normalizedPriorityInput == "l" -> "LOW"
                else -> "MEDIUM"
            }
            onReminderPriorityChange(priority)
            onClearInput()
            val question = "Sound preference? (ring, vibrate, silent)"
            onQuestionChange(question)
            onAddMessage(false, question)
            onStateChange(ChatState.ASKING_SOUND)
        }
        
        ChatState.ASKING_SOUND -> {
            // User provided sound preference
            onAddMessage(true, input)
            val sound = when (input.lowercase()) {
                "silent", "s" -> "silent"
                "vibrate", "v" -> "vibrate"
                else -> "ring"
            }
            onReminderSoundChange(sound)
            onClearInput()
            
            // Now create the reminder with all collected info
            onStateChange(ChatState.PROCESSING)
            val question = if (selectedApp != null) {
                "Opening ${selectedApp.appName} at scheduled time..."
            } else {
                "Creating your reminder..."
            }
            onQuestionChange(question)
            onAddMessage(false, question)
            
            try {
                var triggerTime: Long
                var title: String
                
                if (selectedApp != null) {
                    // App automation flow - parse time with AI
                    val fullInput = "Open ${selectedApp.appName} at $reminderTime"
                    val result = geminiAIService.parseReminder(fullInput, viewModel.currentUserId.value)
                    result.onSuccess { parsedReminder ->
                        android.util.Log.d("TasksScreen", "Parsed: ${parsedReminder.title}, time: ${parsedReminder.triggerTime}")
                        
                        val currentTime = System.currentTimeMillis()
                        triggerTime = if (parsedReminder.triggerTime > currentTime + 60000) {
                            parsedReminder.triggerTime
                        } else {
                            val calendar = java.util.Calendar.getInstance()
                            calendar.add(java.util.Calendar.MINUTE, 2)
                            calendar.timeInMillis
                        }
                        
                        title = "Open ${selectedApp.appName}"
                        
                        val finalPriority = when (reminderPriority) {
                            "URGENT" -> ReminderPriority.URGENT
                            "HIGH" -> ReminderPriority.HIGH
                            "LOW" -> ReminderPriority.LOW
                            else -> ReminderPriority.MEDIUM
                        }
                        
                        // Calculate endTime (30 minutes after open time by default)
                        val endTime = triggerTime + (30 * 60 * 1000)
                        
                        viewModel.createReminder(
                            title = title,
                            description = "",
                            triggerTime = triggerTime,
                            priority = finalPriority,
                            isRecurring = false,
                            appPackageName = selectedApp.packageName,
                            endTime = endTime
                        )
                        
                        val successMsg = "Reminder set to open ${selectedApp.appName}! Anything else?"
                        onQuestionChange(successMsg)
                        onAddMessage(false, successMsg)
                        onClearInput()
                        
                        // Reset conversational state
                        onReminderTitleChange("")
                        onReminderTimeChange(null)
                        onReminderPriorityChange("MEDIUM")
                        onReminderSoundChange("sound")
                        onSelectedAppChange(null)
                        
                        kotlinx.coroutines.delay(1500)
                        onStateChange(ChatState.IDLE)
                    }.onFailure { error ->
                        android.util.Log.e("TasksScreen", "AI parsing failed: ${error.message}", error)
                        // Fallback
                        val calendar = java.util.Calendar.getInstance()
                        calendar.add(java.util.Calendar.MINUTE, 2)
                        triggerTime = calendar.timeInMillis
                        title = "Open ${selectedApp.appName}"
                        
                        val finalPriority = when (reminderPriority) {
                            "URGENT" -> ReminderPriority.URGENT
                            "HIGH" -> ReminderPriority.HIGH
                            "LOW" -> ReminderPriority.LOW
                            else -> ReminderPriority.MEDIUM
                        }
                        
                        val endTime = triggerTime + (30 * 60 * 1000)
                        
                        viewModel.createReminder(
                            title = title,
                            description = "",
                            triggerTime = triggerTime,
                            priority = finalPriority,
                            isRecurring = false,
                            appPackageName = selectedApp.packageName,
                            endTime = endTime
                        )
                        
                        val fallbackMsg = "Reminder created! What else?"
                        onQuestionChange(fallbackMsg)
                        onAddMessage(false, fallbackMsg)
                        onClearInput()
                        
                        onReminderTitleChange("")
                        onReminderTimeChange(null)
                        onReminderPriorityChange("MEDIUM")
                        onReminderSoundChange("sound")
                        onSelectedAppChange(null)
                        
                        kotlinx.coroutines.delay(1500)
                        onStateChange(ChatState.IDLE)
                    }
                } else {
                    // Normal reminder flow
                    if (explicitTriggerTime != null) {
                        // Exact time from English/Hindi explicit clock parsing OR Gemini pre-parse in IDLE
                        triggerTime = explicitTriggerTime
                        title = reminderTitle
                    } else {
                        val fullInput = "Remind me: $reminderTitle at $reminderTime"
                        val parsedReminder = geminiAIService
                            .parseReminder(fullInput, viewModel.currentUserId.value)
                            .getOrNull()
                        val currentTime = System.currentTimeMillis()
                        triggerTime = if (parsedReminder != null && parsedReminder.triggerTime > currentTime + 60000) {
                            parsedReminder.triggerTime
                        } else {
                            val calendar = java.util.Calendar.getInstance()
                            calendar.add(java.util.Calendar.MINUTE, 2)
                            calendar.timeInMillis
                        }
                        title = reminderTitle.ifBlank { parsedReminder?.title ?: reminderTitle }
                    }
                        
                        val finalPriority = when (reminderPriority) {
                            "URGENT" -> ReminderPriority.URGENT
                            "HIGH" -> ReminderPriority.HIGH
                            "LOW" -> ReminderPriority.LOW
                            else -> ReminderPriority.MEDIUM
                        }
                        
                        viewModel.createReminder(
                            title = title,
                            description = "",
                            triggerTime = triggerTime,
                            priority = finalPriority,
                            isRecurring = false
                        )
                        
                        val successMsg = "Reminder set! Anything else?"
                        onQuestionChange(successMsg)
                        onAddMessage(false, successMsg)
                        onClearInput()
                        
                        onReminderTitleChange("")
                        onReminderTimeChange(null)
                        onExplicitTriggerTimeChange(null)
                        onReminderPriorityChange("MEDIUM")
                        onReminderSoundChange("sound")
                        
                        kotlinx.coroutines.delay(1500)
                        onStateChange(ChatState.IDLE)
                }
            } catch (e: Exception) {
                android.util.Log.e("TasksScreen", "Exception: ${e.message}", e)
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.MINUTE, 2)
                val triggerTime = calendar.timeInMillis
                val title = if (selectedApp != null) "Open ${selectedApp.appName}" else reminderTitle
                
                val finalPriority = when (reminderPriority) {
                    "URGENT" -> ReminderPriority.URGENT
                    "HIGH" -> ReminderPriority.HIGH
                    "LOW" -> ReminderPriority.LOW
                    else -> ReminderPriority.MEDIUM
                }
                
                val appPackageName = selectedApp?.packageName
                val endTime = if (selectedApp != null) triggerTime + (30 * 60 * 1000) else null
                
                viewModel.createReminder(
                    title = title,
                    description = "",
                    triggerTime = triggerTime,
                    priority = finalPriority,
                    isRecurring = false,
                    appPackageName = appPackageName,
                    endTime = endTime
                )
                
                val errorMsg = "Done! Need another reminder?"
                onQuestionChange(errorMsg)
                onAddMessage(false, errorMsg)
                onClearInput()
                
                onReminderTitleChange("")
                onReminderTimeChange(null)
                onExplicitTriggerTimeChange(null)
                onReminderPriorityChange("MEDIUM")
                onReminderSoundChange("sound")
                onSelectedAppChange(null)
                
                kotlinx.coroutines.delay(1500)
                onStateChange(ChatState.IDLE)
            }
        }
        
        else -> {
            // Direct input processing (fallback)
            processReminderInput(
                input = input,
                geminiAIService = geminiAIService,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                onStateChange = onStateChange,
                onClearInput = onClearInput,
                onQuestionChange = onQuestionChange
            )
        }
    }
}

/**
 * Helper function to process reminder input via Gemini AI (fallback)
 */
private fun processReminderInput(
    input: String,
    geminiAIService: GeminiAIService,
    viewModel: ReminderViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStateChange: (ChatState) -> Unit,
    onClearInput: () -> Unit,
    onQuestionChange: (String) -> Unit
) {
    android.util.Log.d("TasksScreen", "Processing input: $input")
    onStateChange(ChatState.PROCESSING)
    onQuestionChange("Creating your reminder...")
    
    coroutineScope.launch {
        try {
            val result = geminiAIService.parseReminder(input, viewModel.currentUserId.value)
            result.onSuccess { parsedReminder ->
                android.util.Log.d("TasksScreen", "Parsed: ${parsedReminder.title}, time: ${parsedReminder.triggerTime}")
                // Only use fallback if AI didn't provide a valid time (more than 1 minute from now)
                val currentTime = System.currentTimeMillis()
                val triggerTime: Long = if (parsedReminder.triggerTime > currentTime + 60000) {
                    parsedReminder.triggerTime  // AI provided valid time
                } else {
                    // AI didn't provide valid time, use fallback
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.MINUTE, 2)
                    calendar.timeInMillis
                }
                
                viewModel.createReminder(
                    title = parsedReminder.title,
                    description = parsedReminder.description,
                    triggerTime = triggerTime,
                    priority = when(parsedReminder.priority) {
                        "URGENT" -> ReminderPriority.URGENT
                        "HIGH" -> ReminderPriority.HIGH
                        "LOW" -> ReminderPriority.LOW
                        else -> ReminderPriority.MEDIUM
                    },
                    isRecurring = parsedReminder.isRecurring
                )
                onQuestionChange("Reminder set! Anything else?")
                onClearInput()
                kotlinx.coroutines.delay(1500)
                onStateChange(ChatState.IDLE)
            }.onFailure { error ->
                android.util.Log.e("TasksScreen", "AI parsing failed: ${error.message}", error)
                // Only use fallback when AI completely fails
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.MINUTE, 2)
                viewModel.createReminder(
                    title = input,
                    description = "",
                    triggerTime = calendar.timeInMillis,
                    priority = ReminderPriority.MEDIUM,
                    isRecurring = false
                )
                onQuestionChange("Reminder created! What else?")
                onClearInput()
                kotlinx.coroutines.delay(1500)
                onStateChange(ChatState.IDLE)
            }
        } catch (e: Exception) {
            android.util.Log.e("TasksScreen", "Exception: ${e.message}", e)
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.MINUTE, 2)
            viewModel.createReminder(
                title = input,
                description = "",
                triggerTime = calendar.timeInMillis,
                priority = ReminderPriority.MEDIUM,
                isRecurring = false
            )
            onQuestionChange("Done! Need another reminder?")
            onClearInput()
            kotlinx.coroutines.delay(1500)
            onStateChange(ChatState.IDLE)
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Theme",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOption(
                    title = "Follow System",
                    description = "Use system dark/light mode setting",
                    icon = Icons.Default.Settings,
                    isSelected = currentTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
                
                ThemeOption(
                    title = "Light Mode",
                    description = "Always use light theme",
                    icon = Icons.Default.Star,
                    isSelected = currentTheme == "light",
                    onClick = { onThemeSelected("light") }
                )
                
                ThemeOption(
                    title = "Dark Mode",
                    description = "Always use dark theme",
                    icon = Icons.Default.Settings,
                    isSelected = currentTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ThemeOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SoundSelectionDialog(
    currentUseSystemDefault: Boolean,
    onSoundSelected: (useSystemDefault: Boolean, customUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf(if (currentUseSystemDefault) "system" else "custom") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Notification Sound",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SoundOption(
                    title = "System Default",
                    description = "Use system notification sound",
                    icon = Icons.Default.Notifications,
                    isSelected = selectedOption == "system",
                    onClick = { selectedOption = "system" }
                )
                
                SoundOption(
                    title = "System Alarm",
                    description = "Use system alarm sound",
                    icon = Icons.Default.Settings,
                    isSelected = selectedOption == "alarm",
                    onClick = { selectedOption = "alarm" }
                )
                
                SoundOption(
                    title = "Silent",
                    description = "Vibration only, no sound",
                    icon = Icons.Default.Settings,
                    isSelected = selectedOption == "silent",
                    onClick = { selectedOption = "silent" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedOption) {
                        "system" -> onSoundSelected(true, null)
                        "alarm" -> onSoundSelected(true, "alarm")
                        "silent" -> onSoundSelected(false, null)
                    }
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SoundOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


/**
 * Calendar View with date selection and reminder indicators
 */
@Composable
fun CalendarView(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    datesWithReminders: Set<Long>,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { Calendar.getInstance() }
    var displayMonth by remember { mutableStateOf(currentMonth.clone() as Calendar) }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    displayMonth = (displayMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
                }
                
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(displayMonth.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = {
                    displayMonth = (displayMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Calendar grid
            val firstDayOfMonth = (displayMonth.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = displayMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val totalCells = ((startDayOfWeek + daysInMonth + 6) / 7) * 7
            
            Column {
                for (week in 0 until (totalCells / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val cellIndex = week * 7 + dayOfWeek
                            val dayOfMonth = cellIndex - startDayOfWeek + 1
                            
                            if (dayOfMonth in 1..daysInMonth) {
                                val dateCalendar = (displayMonth.clone() as Calendar).apply {
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val dateMillis = dateCalendar.timeInMillis
                                val hasReminder = datesWithReminders.contains(dateMillis)
                                val isSelected = selectedDate == dateMillis
                                val isToday = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis == dateMillis
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onDateSelected(dateMillis) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (hasReminder) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.tertiary
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Progress Report Dialog with analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressReportDialog(
    reminders: List<Reminder>,
    onDismiss: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("Weekly") } // Weekly, Monthly, Custom
    var showDateRangePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }.timeInMillis) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Filter reminders by selected period
    val filteredReminders = remember(reminders, selectedPeriod, startDate, endDate) {
        val now = Calendar.getInstance()
        when (selectedPeriod) {
            "Weekly" -> {
                val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }
                reminders.filter { it.triggerTime >= weekAgo.timeInMillis && it.triggerTime <= now.timeInMillis }
            }
            "Monthly" -> {
                val monthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                reminders.filter { it.triggerTime >= monthAgo.timeInMillis && it.triggerTime <= now.timeInMillis }
            }
            "Custom" -> {
                reminders.filter { it.triggerTime >= startDate && it.triggerTime <= endDate }
            }
            else -> reminders
        }
    }
    
    // Calculate statistics
    val totalReminders = filteredReminders.size
    val completedReminders = filteredReminders.count { it.status.name == "COMPLETED" }
    val completionRate = if (totalReminders > 0) (completedReminders.toFloat() / totalReminders * 100).toInt() else 0
    val urgentCount = filteredReminders.count { it.priority.name == "URGENT" }
    val highCount = filteredReminders.count { it.priority.name == "HIGH" }
    val mediumCount = filteredReminders.count { it.priority.name == "MEDIUM" }
    val lowCount = filteredReminders.count { it.priority.name == "LOW" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Progress Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Weekly", "Monthly", "Custom").forEach { period ->
                            FilterChip(
                                selected = selectedPeriod == period,
                                onClick = {
                                    selectedPeriod = period
                                    if (period == "Custom") {
                                        showDateRangePicker = true
                                    }
                                },
                                label = { Text(period, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }
                
                // Date range display for custom
                if (selectedPeriod == "Custom") {
                    item {
                        Text(
                            text = "${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(startDate))} - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(endDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Overall statistics
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Overall Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Total Reminders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        totalReminders.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text(
                                        "Completed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        completedReminders.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Column {
                                    Text(
                                        "Completion Rate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$completionRate%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Priority breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Priority Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            PriorityBar("Urgent", urgentCount, totalReminders, MaterialTheme.colorScheme.error)
                            PriorityBar("High", highCount, totalReminders, MaterialTheme.colorScheme.tertiary)
                            PriorityBar("Medium", mediumCount, totalReminders, MaterialTheme.colorScheme.primary)
                            PriorityBar("Low", lowCount, totalReminders, MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                
                // Daily average
                item {
                    val days = when (selectedPeriod) {
                        "Weekly" -> 7
                        "Monthly" -> 30
                        "Custom" -> ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                        else -> 7
                    }
                    val dailyAverage = if (days > 0) totalReminders.toFloat() / days else 0f
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Daily Average",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    String.format("%.1f reminders/day", dailyAverage),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PriorityBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$count",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}


/**
 * Calendar View Dialog - Full screen dialog with calendar and filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewDialog(
    reminders: List<Reminder>,
    onDismiss: () -> Unit,
    viewModel: ReminderViewModel
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Get dates with reminders for calendar dots
    val datesWithReminders = remember(reminders) {
        reminders.map { reminder ->
            val cal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()
    }
    
    // Filter reminders
    val filteredReminders = remember(reminders, selectedDate, selectedFilter) {
        when {
            selectedDate != null -> {
                val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                reminders.filter { reminder ->
                    val reminderCal = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    selectedCal.get(Calendar.YEAR) == reminderCal.get(Calendar.YEAR) &&
                    selectedCal.get(Calendar.DAY_OF_YEAR) == reminderCal.get(Calendar.DAY_OF_YEAR)
                }
            }
            selectedFilter != "All" -> {
                val today = Calendar.getInstance()
                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
                
                reminders.filter { reminder ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = reminder.triggerTime }
                    when (selectedFilter) {
                        "Yesterday" -> calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                                      calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
                        "Today" -> calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                  calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                        "Tomorrow" -> calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                                     calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
                        "This Week" -> calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                      calendar.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
                        "Later" -> calendar.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
                                  (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                   calendar.get(Calendar.WEEK_OF_YEAR) > today.get(Calendar.WEEK_OF_YEAR))
                        else -> true
                    }
                }
            }
            else -> reminders
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Calendar & Filters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calendar
                CalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        selectedDate = if (selectedDate == date) null else date
                        selectedFilter = "All"
                    },
                    datesWithReminders = datesWithReminders,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Filter chips
                val filters = listOf("All", "Yesterday", "Today", "Tomorrow", "This Week", "Later")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters.size) { index ->
                        val filter = filters[index]
                        FilterChip(
                            selected = selectedFilter == filter && selectedDate == null,
                            onClick = {
                                selectedFilter = filter
                                selectedDate = null
                            },
                            label = { Text(filter, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                
                // Reminders count
                Text(
                    text = "${filteredReminders.size} reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

