package com.readbridge.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.readbridge.app.ui.auth.LoginScreen
import com.readbridge.app.ui.home.HomeScreen
import com.readbridge.app.ui.screens.PlaceholderScreen

/**
 * Root navigation graph. The authenticated vs unauthenticated split is decided by the
 * caller via [startDestination] (see MainActivity's auth gate). Reader/Settings are
 * still placeholders until later phases.
 */
@Composable
fun ReadBridgeNavGraph(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Destinations.LOGIN) {
            LoginScreen()
        }
        composable(Destinations.ARTICLE_LIST) {
            HomeScreen(
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }
        composable(Destinations.READER) {
            PlaceholderScreen(title = "Leitor")
        }
        composable(Destinations.SETTINGS) {
            PlaceholderScreen(
                title = "Configurações",
                actionLabel = "Voltar",
                onAction = { navController.popBackStack() },
            )
        }
    }
}
