package com.readbridge.app.ui.navigation

/**
 * Navigation routes for the app. Screens are stubbed in Phase 0 and get real
 * implementations in later phases (Login → List → Reader → Settings).
 */
object Destinations {
    const val LOGIN = "login"
    const val ARTICLE_LIST = "articles"
    const val READER = "reader/{entryId}"
    const val SETTINGS = "settings"

    fun reader(entryId: Long): String = "reader/$entryId"
}
