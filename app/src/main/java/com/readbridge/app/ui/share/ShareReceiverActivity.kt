package com.readbridge.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.readbridge.app.domain.article.usecase.AddArticleUseCase
import com.readbridge.app.domain.auth.AuthRepository
import com.readbridge.app.domain.auth.model.AuthState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Invisible activity that receives shared links (ACTION_SEND) from other apps and queues
 * them into the outbox to be saved on the server. Shows a toast and finishes immediately.
 */
@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {

    @Inject lateinit var addArticle: AddArticleUseCase

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUrl = extractUrl(intent)
        when {
            sharedUrl == null -> {
                toast("Nenhum link para salvar.")
                finish()
            }
            authRepository.authState.value != AuthState.Authenticated -> {
                toast("Faça login no ReadBridge antes de salvar artigos.")
                finish()
            }
            else -> {
                lifecycleScope.launch {
                    addArticle(sharedUrl)
                    toast("Salvo no ReadBridge — sincronizando…")
                    finish()
                }
            }
        }
    }

    private fun extractUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
