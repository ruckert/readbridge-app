package com.readbridge.app.ui.reader

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.text.HtmlCompat
import java.util.Locale

/** Convert article HTML to plain, speakable text. */
fun htmlToSpeechText(html: String): String =
    HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()

/**
 * Text-to-speech for the current article. Tied to the composition: the engine is created
 * lazily and shut down on dispose. [isSpeaking] drives the toolbar's play/stop icon.
 */
class ArticleTtsController {
    private var tts: TextToSpeech? = null
    private var lastUtteranceId: String = ""

    private val _isSpeaking = mutableStateOf(false)
    val isSpeaking: State<Boolean> = _isSpeaking

    internal val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) {
            if (utteranceId == lastUtteranceId) _isSpeaking.value = false
        }

        override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
        }
    }

    internal fun attach(engine: TextToSpeech) {
        tts = engine
    }

    internal fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setLanguage(Locale.getDefault())
        }
    }

    /** Start reading [text], or stop if already speaking. */
    fun toggle(text: String) {
        if (_isSpeaking.value) stop() else speak(text)
    }

    private fun speak(text: String) {
        val engine = tts ?: return
        val chunks = chunk(text)
        if (chunks.isEmpty()) return
        lastUtteranceId = "rb_${chunks.lastIndex}"
        chunks.forEachIndexed { index, piece ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(piece, mode, null, "rb_$index")
        }
        _isSpeaking.value = true
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    internal fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isSpeaking.value = false
    }

    private fun chunk(text: String, max: Int = 3500): List<String> {
        if (text.length <= max) return listOf(text).filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            var end = minOf(start + max, text.length)
            if (end < text.length) {
                val boundary = maxOf(
                    text.lastIndexOf(". ", end),
                    text.lastIndexOf('\n', end),
                )
                if (boundary > start) end = boundary + 1
            }
            text.substring(start, end).trim().takeIf { it.isNotBlank() }?.let(result::add)
            start = end
        }
        return result
    }
}

@Composable
fun rememberArticleTts(): ArticleTtsController {
    val context = LocalContext.current
    val controller = remember { ArticleTtsController() }
    DisposableEffect(controller) {
        val engine = TextToSpeech(context.applicationContext) { status -> controller.onInit(status) }
        engine.setOnUtteranceProgressListener(controller.progressListener)
        controller.attach(engine)
        onDispose { controller.release() }
    }
    return controller
}
