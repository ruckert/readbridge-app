package com.readbridge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readbridge.app.domain.auth.model.AuthState
import com.readbridge.app.ui.navigation.Destinations
import com.readbridge.app.ui.navigation.ReadBridgeNavGraph
import com.readbridge.app.ui.theme.ReadBridgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadBridgeTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val authState by viewModel.authState.collectAsStateWithLifecycle()
                val authenticated = authState == AuthState.Authenticated

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Rebuild the nav graph when auth flips so the start destination and
                    // back stack reset cleanly between login and the main flow.
                    key(authenticated) {
                        ReadBridgeNavGraph(
                            startDestination = if (authenticated) {
                                Destinations.ARTICLE_LIST
                            } else {
                                Destinations.LOGIN
                            },
                        )
                    }
                }
            }
        }
    }
}
