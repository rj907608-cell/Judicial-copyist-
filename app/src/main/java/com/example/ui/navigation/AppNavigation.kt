package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DocumentDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.DocumentViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object DocumentDetail : Screen("document_detail/{documentId}") {
        fun createRoute(documentId: Long) = "document_detail/$documentId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    viewModel: DocumentViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScan = {
                    navController.navigate(Screen.Scanner.route)
                },
                onNavigateToDetail = { docId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(docId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { docId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(docId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.DocumentDetail.route,
            arguments = listOf(
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0L
            DocumentDetailScreen(
                documentId = documentId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
