package com.readbridge.app.domain.auth.usecase

import com.readbridge.app.domain.auth.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.logout()
}
