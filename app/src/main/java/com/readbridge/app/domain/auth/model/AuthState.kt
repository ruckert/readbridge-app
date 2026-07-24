package com.readbridge.app.domain.auth.model

/** Whether an active Wallabag session exists. Drives the app's start destination. */
enum class AuthState {
    Authenticated,
    Unauthenticated,
}
