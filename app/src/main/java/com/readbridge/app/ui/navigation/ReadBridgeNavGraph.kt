package com.readbridge.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.readbridge.app.ui.articles.ArticleListScreen
import com.readbridge.app.ui.auth.LoginScreen
import com.readbridge.app.ui.reader.ReaderScreen
import com.readbridge.app.ui.screens.PlaceholderScreen

/**
 * Root navigation graph. The authenticated vs unauthenticated split is decided by the
 * caller via [startDestination] (see MainActivity's auth gate). The reader (Phase 3) and
 * settings (later) are still placeholders.
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
            ArticleListScreen(
                onArticleClick = { entryId -> navController.navigate(Destinations.reader(entryId)) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }
        composable(
            route = Destinations.READER,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) {
            ReaderScreen(onBack = { navController.popBackStack() })
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
