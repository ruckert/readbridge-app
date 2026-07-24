package com.readbridge.app

import androidx.lifecycle.ViewModel
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Exposes the session state that gates the app between login and the main flow. */
@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState
}
