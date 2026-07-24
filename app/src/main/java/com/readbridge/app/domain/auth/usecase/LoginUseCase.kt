package com.readbridge.app.domain.auth.usecase

import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.LoginResult
import com.readbridge.app.domain.auth.model.ServerConfig
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        config: ServerConfig,
        username: String,
        password: String,
    ): LoginResult = repository.login(config, username, password)
}
