package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.viewmodel.GameBoosterViewModel

// Navigation Route Constants
const val ROUTE_DASHBOARD = "dashboard"
const val ROUTE_DNS = "dns"
const val ROUTE_FPS = "fps"
const val ROUTE_LAUNCHER = "launcher"
const val ROUTE_SHIZUKU = "shizuku"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_SETTINGS = "settings"

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen(ROUTE_DASHBOARD, "Engine", Icons.Default.OfflineBolt)
    object Dns : Screen(ROUTE_DNS, "Network", Icons.Default.Language)
    object Fps : Screen(ROUTE_FPS, "Frame", Icons.Default.Speed)
    object Launcher : Screen(ROUTE_LAUNCHER, "Launcher", Icons.Default.RocketLaunch)
    object Shizuku : Screen(ROUTE_SHIZUKU, "Shizuku", Icons.Default.DeveloperMode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: GameBoosterViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_DASHBOARD

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Elegant top app bar with actions for Analytics and Settings
            TopAppBar(
                title = {
                    Text(
                        text = "NothinG",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(ROUTE_ANALYTICS) }
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Analytics",
                            tint = if (currentRoute == ROUTE_ANALYTICS) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(ROUTE_SETTINGS) }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentRoute == ROUTE_SETTINGS) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Uncluttered, modern active-pill bottom bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val navigationItems = listOf(
                    Screen.Dashboard,
                    Screen.Dns,
                    Screen.Fps,
                    Screen.Launcher,
                    Screen.Shizuku
                )

                navigationItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.secondary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLauncher = { navController.navigate(Screen.Launcher.route) }
                )
            }
            composable(ROUTE_DNS) {
                DnsBoosterScreen(viewModel = viewModel)
            }
            composable(ROUTE_FPS) {
                FpsStabilityScreen(viewModel = viewModel)
            }
            composable(ROUTE_LAUNCHER) {
                GameLauncherScreen(viewModel = viewModel)
            }
            composable(ROUTE_SHIZUKU) {
                ShizukuScreen(viewModel = viewModel)
            }
            composable(ROUTE_ANALYTICS) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
