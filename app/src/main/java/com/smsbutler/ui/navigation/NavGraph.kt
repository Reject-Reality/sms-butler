package com.smsbutler.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smsbutler.ui.screen.detail.DetailScreen
import com.smsbutler.ui.screen.home.HomeScreen
import com.smsbutler.ui.screen.search.SearchScreen
import com.smsbutler.ui.screen.settings.SettingsScreen
import com.smsbutler.ui.screen.stats.StatsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "记录", Icons.Filled.Home)
    object Stats : Screen("stats", "统计", Icons.Filled.BarChart)
    object Search : Screen("search", "搜索", Icons.Filled.ManageSearch)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    object Detail : Screen("detail/{phoneNumber}", "详情", Icons.Filled.Home)
}

@Composable
fun SmsButlerNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomScreens = listOf(Screen.Home, Screen.Stats, Screen.Search, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onRecordClick = { record ->
                    navController.navigate("detail/${record.phoneNumber}")
                })
            }
            composable(Screen.Search.route) {
                SearchScreen(onRecordClick = { record ->
                    navController.navigate("detail/${record.phoneNumber}")
                })
            }
            composable(Screen.Stats.route) {
                StatsScreen(onPhoneClick = { phone ->
                    navController.navigate("detail/$phone")
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
