package com.readbridge.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.readbridge.app.ui.screens.PlaceholderScreen

/**
 * Root navigation graph. In Phase 0 every destination shows a placeholder so the
 * app builds, runs, and navigates end-to-end. Real screens land in later phases.
 */
@Composable
fun ReadBridgeNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.ARTICLE_LIST,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Destinations.LOGIN) {
            PlaceholderScreen(title = "Login (Wallabag)")
        }
        composable(Destinations.ARTICLE_LIST) {
            PlaceholderScreen(
                title = "ReadBridge",
                subtitle = "Scaffold da Fase 0 — lista de artigos vem na Fase 2.",
                actionLabel = "Abrir Configurações",
                onAction = { navController.navigate(Destinations.SETTINGS) },
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
