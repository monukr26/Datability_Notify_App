package com.mks.databilitynotifyapp.presentation.navigation

sealed class Screen(val route: String) {
    data object Inbox : Screen("inbox")
    data object Details : Screen("details/{notificationId}") {
        fun createRoute(notificationId: String): String = "details/$notificationId"
    }
}

