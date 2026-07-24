package com.readbridge.app.ui.reader

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val article by viewModel.article.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val initialRatio by viewModel.initialScrollRatio.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val palette = remember(prefs, systemDark) { resolveReadingPalette(prefs, systemDark) }
    val containerColor = Color(palette.background.toColorInt())
    val contentColor = Color(palette.text.toColorInt())

    val context = LocalContext.current
    val tts = rememberArticleTts()
    val isSpeaking by tts.isSpeaking
    val speechText = remember(article?.id) {
        article?.let { htmlToSpeechText(listOfNotNull(it.title, it.contentHtml).joinToString(". ")) }.orEmpty()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    var showControls by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    val current = article
                    IconButton(
                        onClick = { if (speechText.isNotBlank()) tts.toggle(speechText) },
                        enabled = speechText.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isSpeaking) "Parar leitura" else "Ouvir artigo",
                        )
                    }
                    IconButton(onClick = viewModel::toggleStar) {
                        Icon(
                            imageVector = if (current?.isStarred == true) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Favoritar",
                        )
                    }
                    IconButton(onClick = viewModel::toggleArchive) {
                        Icon(
                            imageVector = if (current?.isArchived == true) Icons.Filled.Unarchive else Icons.Filled.Archive,
                            contentDescription = "Arquivar",
                        )
                    }
                    TextButton(onClick = { showControls = true }) { Text("Aa") }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Abrir original") },
                            onClick = {
                                menuExpanded = false
                                current?.url?.let { context.openUrl(it) }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Compartilhar") },
                            onClick = {
                                menuExpanded = false
                                current?.url?.let { context.shareUrl(it) }
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    navigationIconContentColor = contentColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val current = article
            val ratio = initialRatio
            when {
                current == null || ratio == null -> CircularProgressIndicator(color = contentColor)
                current.contentHtml.isBlank() -> Text(
                    text = "Conteúdo indisponível offline. Sincronize e tente novamente.",
                    color = contentColor,
                    modifier = Modifier.padding(24.dp),
                )
                else -> ArticleWebView(
                    title = current.title.orEmpty(),
                    contentHtml = current.contentHtml,
                    prefs = prefs,
                    palette = palette,
                    initialScrollRatio = ratio,
                    onSaveProgress = viewModel::saveProgress,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (showControls) {
        ModalBottomSheet(
            onDismissRequest = { showControls = false },
            sheetState = sheetState,
        ) {
            TypographyControls(
                prefs = prefs,
                onChange = viewModel::updatePreferences,
                modifier = Modifier
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            )
        }
    }
}

private fun android.content.Context.openUrl(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
}

private fun android.content.Context.shareUrl(url: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    runCatching { startActivity(Intent.createChooser(send, "Compartilhar")) }
}
