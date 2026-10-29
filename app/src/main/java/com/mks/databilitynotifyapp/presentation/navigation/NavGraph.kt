package com.mks.databilitynotifyapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mks.databilitynotifyapp.di.AppContainer
import com.mks.databilitynotifyapp.presentation.DetailsViewModelFactory
import com.mks.databilitynotifyapp.presentation.InboxViewModelFactory
import com.mks.databilitynotifyapp.presentation.details.NotificationDetailsScreen
import com.mks.databilitynotifyapp.presentation.details.NotificationDetailsViewModel
import com.mks.databilitynotifyapp.presentation.inbox.InboxScreen
import com.mks.databilitynotifyapp.presentation.inbox.InboxViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inbox.route,
        modifier = modifier
    ) {
        composable(route = Screen.Inbox.route) {
            val inboxViewModel: InboxViewModel = viewModel(
                factory = InboxViewModelFactory(appContainer.notificationRepository)
            )
            InboxScreen(
                viewModel = inboxViewModel,
                onNotificationClick = { notificationId ->
                    navController.navigate(Screen.Details.createRoute(notificationId))
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("notificationId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId").orEmpty()
            val detailsViewModel: NotificationDetailsViewModel = viewModel(
                factory = DetailsViewModelFactory(
                    notificationId = notificationId,
                    notificationRepository = appContainer.notificationRepository
                )
            )
            NotificationDetailsScreen(
                viewModel = detailsViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

