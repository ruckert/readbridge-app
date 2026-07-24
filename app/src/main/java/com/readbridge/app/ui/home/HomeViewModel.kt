package com.readbridge.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logout: LogoutUseCase,
    authRepository: AuthRepository,
) : ViewModel() {

    val serverUrl: String? = authRepository.currentServerConfig()?.serverUrl

    fun onLogout() {
        viewModelScope.launch { logout() }
    }
}
