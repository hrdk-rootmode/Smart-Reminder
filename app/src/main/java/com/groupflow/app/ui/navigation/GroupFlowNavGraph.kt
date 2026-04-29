package com.groupflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.groupflow.app.service.FirebaseAuthService
import com.groupflow.app.ui.screens.GroupsScreen
import com.groupflow.app.ui.screens.TasksScreen
import com.groupflow.app.ui.screens.ChatsScreen
import com.groupflow.app.ui.screens.ProfileScreen
import com.groupflow.app.ui.screens.ChatDetailScreen
import com.groupflow.app.ui.screens.SignInScreen
import com.groupflow.app.ui.screens.AddReminderDialog
import com.groupflow.app.ui.viewmodel.ReminderViewModel
import com.groupflow.app.data.local.entity.ReminderPriority
import java.util.Calendar

sealed class Screen(val route: String, val title: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    object SignIn : Screen("signin", "Sign In", Icons.Filled.Person, Icons.Outlined.Person)
    object Groups : Screen("groups", "Groups", Icons.Filled.Person, Icons.Outlined.Person)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Chats : Screen("chats", "Chats", Icons.Filled.Send, Icons.Outlined.Send)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    object ChatDetail : Screen("chat/{groupId}", "Chat", Icons.Default.Send, Icons.Default.Send) { fun createRoute(groupId: String) = "chat/$groupId" }
}

// Guest users only see Tasks tab, logged-in users see all tabs
fun getBottomNavScreens(isLoggedIn: Boolean) = if (isLoggedIn) {
    listOf(
        Screen.Tasks,
        Screen.Groups,
        Screen.Chats,
        Screen.Profile
    )
} else {
    listOf(Screen.Tasks, Screen.Profile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFlowNavGraph(
    navController: NavHostController = rememberNavController(),
    firebaseAuthService: FirebaseAuthService
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val currentUser by firebaseAuthService.currentUser.collectAsState(initial = null)
    val reminderViewModel: ReminderViewModel = viewModel()

    // Update user ID in ReminderViewModel when user changes
    currentUser?.let {
        reminderViewModel.setUserId(it.uid)
    }

    // Check if user is authenticated
    val startDestination = if (currentUser != null) Screen.Tasks.route else Screen.Tasks.route
    val isLoggedIn = currentUser != null
    val bottomNavScreens = getBottomNavScreens(isLoggedIn)
    
    // Add reminder dialog state
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentRoute == Screen.Tasks.route) {
                TopAppBar(
                    title = { Text("Reminders") },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Tasks.route) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        },
        bottomBar = {
            if (currentRoute in bottomNavScreens.map { it.route }) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentRoute == screen.route) screen.iconSelected else screen.iconUnselected,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.SignIn.route) {
                SignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onContinueAsGuest = {
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    firebaseAuthService = firebaseAuthService
                )
            }
            composable(Screen.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId -> navController.navigate(Screen.ChatDetail.createRoute(groupId)) }
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = reminderViewModel,
                    onAddReminder = { showAddDialog = true },
                    onSignIn = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    isGuest = !isLoggedIn
                )
            }
            composable(Screen.Chats.route) {
                ChatsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    firebaseAuthService = firebaseAuthService,
                    onLogout = {
                        firebaseAuthService.signOut()
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onSignIn = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ChatDetail.route) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                ChatDetailScreen(
                    groupId = groupId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, triggerTime, priority ->
                reminderViewModel.createReminder(title, description, triggerTime, priority)
                showAddDialog = false
            }
        )
    }
}
