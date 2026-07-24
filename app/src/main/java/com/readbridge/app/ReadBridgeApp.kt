package com.readbridge.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] bootstraps the Hilt dependency graph
 * used across the app (repositories, networking, sync — added in later phases).
 */
@HiltAndroidApp
class ReadBridgeApp : Application()
