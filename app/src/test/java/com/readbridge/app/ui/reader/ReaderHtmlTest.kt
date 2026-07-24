package com.readbridge.app.ui.reader

import com.readbridge.app.domain.reader.model.ContentWidth
import com.readbridge.app.domain.reader.model.ReaderFontFamily
import com.readbridge.app.domain.reader.model.ReaderTextAlign
import com.readbridge.app.domain.reader.model.ReadingPreferences
import com.readbridge.app.domain.reader.model.ReadingTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderHtmlTest {

    private val palette =
        resolveReadingPalette(ReadingPreferences(theme = ReadingTheme.Sepia), systemDark = false)

    @Test
    fun `css variables reflect preferences`() {
        val prefs = ReadingPreferences(
            fontSizeSp = 22,
            lineHeight = 1.8f,
            contentWidth = ContentWidth.Wide,
            textAlign = ReaderTextAlign.Justify,
            hyphenation = true,
            fontFamily = ReaderFontFamily.Monospace,
        )
        val vars = ReaderHtml.cssVariables(prefs, palette)

        assertEquals("22px", vars["--rb-font-size"])
        assertEquals("1.8", vars["--rb-line-height"])
        assertEquals("50rem", vars["--rb-content-width"])
        assertEquals("justify", vars["--rb-text-align"])
        assertEquals("auto", vars["--rb-hyphens"])
        assertEquals(palette.background, vars["--rb-bg"])
        assertTrue(vars["--rb-font-family"]!!.contains("monospace"))
    }

    @Test
    fun `js updater is self-invoking and sets every variable`() {
        val prefs = ReadingPreferences()
        val js = ReaderHtml.cssVariablesJs(prefs, palette)

        assertTrue(js.startsWith("(function()"))
        assertTrue(js.trimEnd().endsWith("})();"))
        // one setProperty call per variable
        val calls = Regex("setProperty\\(").findAll(js).count()
        assertEquals(ReaderHtml.cssVariables(prefs, palette).size, calls)
    }

    @Test
    fun `document embeds content and escapes the title`() {
        val html = ReaderHtml.buildDocument(
            title = "A & B <script>",
            contentHtml = "<p>hello</p>",
            prefs = ReadingPreferences(),
            palette = palette,
        )
        assertTrue(html.contains("<p>hello</p>"))
        assertTrue(html.contains("A &amp; B &lt;script&gt;"))
    }
}
