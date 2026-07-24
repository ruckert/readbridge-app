package com.readbridge.app.di

import javax.inject.Qualifier

/** OkHttp client for unauthenticated OAuth calls (no token authenticator → no recursion). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

/** OkHttp client for authenticated API calls (bearer token + 401 refresh). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiClient
