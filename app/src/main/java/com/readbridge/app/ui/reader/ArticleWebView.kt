package com.readbridge.app.ui.reader

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.readbridge.app.domain.reader.model.ReadingPreferences

/**
 * Renders the article HTML and applies preference changes live via CSS variables (no reload).
 * Restores the saved scroll position after load and persists it on pause/dispose.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleWebView(
    title: String,
    contentHtml: String,
    prefs: ReadingPreferences,
    palette: ReadingThemePalette,
    initialScrollRatio: Float,
    onSaveProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val latestPrefs = rememberUpdatedState(prefs)
    val latestPalette = rememberUpdatedState(palette)
    val latestOnSave = rememberUpdatedState(onSaveProgress)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                webViewRef.value?.let { latestOnSave.value(it.currentScrollRatio()) }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            webViewRef.value?.let { latestOnSave.value(it.currentScrollRatio()) }
            lifecycleOwner.lifecycle.removeObserver(observer)
            webViewRef.value?.destroy()
            webViewRef.value = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                setBackgroundColor(palette.background.toColorInt())
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (initialScrollRatio > 0f) {
                            view.postDelayed({ view.scrollToRatio(initialScrollRatio) }, 200)
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        // Open in-article links in the browser rather than inside the reader.
                        runCatching {
                            view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
                        }
                        return true
                    }
                }
                loadDataWithBaseURL(
                    null,
                    ReaderHtml.buildDocument(title, contentHtml, prefs, palette),
                    "text/html",
                    "utf-8",
                    null,
                )
                webViewRef.value = this
            }
        },
        update = { webView ->
            webView.setBackgroundColor(latestPalette.value.background.toColorInt())
            webView.evaluateJavascript(
                ReaderHtml.cssVariablesJs(latestPrefs.value, latestPalette.value),
                null,
            )
        },
    )
}

private fun WebView.currentScrollRatio(): Float {
    val range = contentHeight * resources.displayMetrics.density - height
    return if (range > 0) (scrollY / range).coerceIn(0f, 1f) else 0f
}

private fun WebView.scrollToRatio(ratio: Float) {
    val range = contentHeight * resources.displayMetrics.density - height
    if (range > 0) scrollTo(0, (range * ratio).toInt())
}
