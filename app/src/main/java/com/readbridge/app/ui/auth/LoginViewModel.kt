package com.readbridge.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.LoginResult
import com.readbridge.app.domain.auth.model.ServerConfig
import com.readbridge.app.domain.auth.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val clientId: String = "",
    val clientSecret: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isLoading &&
            serverUrl.isNotBlank() &&
            clientId.isNotBlank() &&
            clientSecret.isNotBlank() &&
            username.isNotBlank() &&
            password.isNotBlank()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(prefill(authRepository.currentServerConfig()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onServerUrlChange(value: String) = _uiState.update { it.copy(serverUrl = value, errorMessage = null) }
    fun onClientIdChange(value: String) = _uiState.update { it.copy(clientId = value, errorMessage = null) }
    fun onClientSecretChange(value: String) = _uiState.update { it.copy(clientSecret = value, errorMessage = null) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    /**
     * Attempt login. On success, [AuthRepository.authState] flips to Authenticated and
     * the app-level auth gate navigates away — this screen does not navigate itself.
     */
    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = login(
                config = ServerConfig(
                    serverUrl = state.serverUrl.trim(),
                    clientId = state.clientId.trim(),
                    clientSecret = state.clientSecret.trim(),
                ),
                username = state.username.trim(),
                password = state.password,
            )
            _uiState.update {
                it.copy(isLoading = false, errorMessage = result.toErrorMessageOrNull())
            }
        }
    }

    private fun prefill(config: ServerConfig?): LoginUiState =
        if (config == null) {
            LoginUiState()
        } else {
            LoginUiState(
                serverUrl = config.serverUrl,
                clientId = config.clientId,
                clientSecret = config.clientSecret,
            )
        }

    private fun LoginResult.toErrorMessageOrNull(): String? = when (this) {
        is LoginResult.Success -> null
        LoginResult.InvalidCredentials -> "Usuário ou senha inválidos."
        LoginResult.InvalidClient -> "Client ID/Secret inválidos. Verifique o cliente de API no Wallabag."
        LoginResult.ServerUnreachable -> "Não foi possível conectar ao servidor. Verifique a URL e sua conexão."
        is LoginResult.Unknown -> "Falha no login: $message"
    }
}
