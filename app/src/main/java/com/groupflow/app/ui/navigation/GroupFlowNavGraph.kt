package com.groupflow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.groupflow.app.ui.screens.GroupsScreen
import com.groupflow.app.ui.screens.TasksScreen
import com.groupflow.app.ui.screens.ChatsScreen
import com.groupflow.app.ui.screens.ProfileScreen
import com.groupflow.app.ui.screens.ChatDetailScreen

sealed class Screen(val route: String, val title: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    object Groups : Screen("groups", "Groups", Icons.Filled.Person, Icons.Outlined.Person)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Chats : Screen("chats", "Chats", Icons.Filled.Send, Icons.Outlined.Send)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    object ChatDetail : Screen("chat/{groupId}", "Chat", Icons.Default.Send, Icons.Default.Send) { fun createRoute(groupId: String) = "chat/$groupId" }
}

val bottomNavScreens = listOf(
    Screen.Groups,
    Screen.Tasks,
    Screen.Chats,
    Screen.Profile
)

@Composable
fun GroupFlowNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    Scaffold(
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
            startDestination = Screen.Groups.route
        ) {
            composable(Screen.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId -> navController.navigate(Screen.ChatDetail.createRoute(groupId)) }
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen()
            }
            composable(Screen.Chats.route) {
                ChatsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
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
}
