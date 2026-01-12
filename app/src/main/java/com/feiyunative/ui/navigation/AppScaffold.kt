package com.feiyunative.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.feiyunative.ui.screen.PlanScreen
import com.feiyunative.ui.screen.FocusTimelineScreen
import com.feiyunative.ui.screen.SettingsScreen

@Composable
fun AppScaffold() {

    val navController = rememberNavController()

    // ✅ 所有 Bottom Tab 统一在这里管理
    val items = listOf(
        BottomNavItem.Plan,
        BottomNavItem.Timeline,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(86.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(item.label) },
                        icon = {} // 暂时不用 icon，保持最稳
                    )
                }
            }
        }
    ) { innerPadding ->

        // ✅ 关键：必须使用 Scaffold 给的 padding
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Timeline.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Plan.route) {
                PlanScreen()
            }
            composable(BottomNavItem.Timeline.route) {
                FocusTimelineScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
