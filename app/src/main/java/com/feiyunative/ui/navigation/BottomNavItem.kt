package com.feiyunative.ui.navigation

sealed class BottomNavItem(
    val route: String,
    val label: String
) {
    object Plan : BottomNavItem(
        route = "plan",
        label = "Plan"
    )

    object Timeline : BottomNavItem(
        route = "timeline",
        label = "Timeline"
    )

    object Settings : BottomNavItem("settings", "Settings")
}
