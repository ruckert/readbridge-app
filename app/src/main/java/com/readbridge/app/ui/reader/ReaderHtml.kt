package com.readbridge.app.ui.reader

import com.readbridge.app.domain.reader.model.ContentWidth
import com.readbridge.app.domain.reader.model.ReaderFontFamily
import com.readbridge.app.domain.reader.model.ReaderFontWeight
import com.readbridge.app.domain.reader.model.ReaderTextAlign
import com.readbridge.app.domain.reader.model.ReadingPreferences

/**
 * Builds the reader WebView document and the JS that updates it live. All styling is driven
 * by CSS custom properties on `:root`, so changing a preference only mutates variables
 * (via [cssVariablesJs]) — no reload, no lost scroll position (PLAN §6-A).
 */
object ReaderHtml {

    /** Ordered map of CSS custom property → value. Pure, so it is unit-tested directly. */
    fun cssVariables(prefs: ReadingPreferences, palette: ReadingThemePalette): Map<String, String> =
        linkedMapOf(
            "--rb-bg" to palette.background,
            "--rb-text" to palette.text,
            "--rb-secondary" to palette.secondary,
            "--rb-link" to palette.link,
            "--rb-block-bg" to palette.blockBackground,
            "--rb-border" to palette.border,
            "--rb-font-size" to "${prefs.fontSizeSp}px",
            "--rb-font-family" to fontFamily(prefs.fontFamily),
            "--rb-font-weight" to fontWeight(prefs.fontWeight),
            "--rb-line-height" to prefs.lineHeight.toString(),
            "--rb-letter-spacing" to "${prefs.letterSpacingEm}em",
            "--rb-content-width" to contentWidth(prefs.contentWidth),
            "--rb-text-align" to textAlign(prefs.textAlign),
            "--rb-hyphens" to if (prefs.hyphenation) "auto" else "manual",
        )

    /** JS snippet that applies all current variables to the live document. */
    fun cssVariablesJs(prefs: ReadingPreferences, palette: ReadingThemePalette): String {
        val body = cssVariables(prefs, palette).entries.joinToString(separator = "") { (name, value) ->
            // Values may contain single quotes (font stacks) → wrap them in double quotes.
            "r.setProperty('$name', \"$value\");"
        }
        return "(function(){var r=document.documentElement.style;$body})();"
    }

    /** Full HTML document with the initial variables inlined on `:root`. */
    fun buildDocument(
        title: String,
        contentHtml: String,
        prefs: ReadingPreferences,
        palette: ReadingThemePalette,
    ): String {
        val rootVars = cssVariables(prefs, palette).entries.joinToString(separator = "\n") { (name, value) ->
            "      $name: $value;"
        }
        val heading = if (title.isBlank()) "" else "<h1>${escapeHtml(title)}</h1>"
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=5">
              <style>
                :root {
$rootVars
                }
                html { -webkit-text-size-adjust: 100%; }
                body {
                  margin: 0;
                  padding: 24px 20px 96px;
                  background: var(--rb-bg);
                  color: var(--rb-text);
                  font-family: var(--rb-font-family);
                  font-size: var(--rb-font-size);
                  font-weight: var(--rb-font-weight);
                  line-height: var(--rb-line-height);
                  letter-spacing: var(--rb-letter-spacing);
                  text-align: var(--rb-text-align);
                  -webkit-hyphens: var(--rb-hyphens);
                  hyphens: var(--rb-hyphens);
                }
                .rb-content {
                  max-width: var(--rb-content-width);
                  margin: 0 auto;
                }
                h1 { font-size: 1.5em; line-height: 1.25; text-align: start; margin: 0 0 0.75em; }
                h2, h3, h4 { line-height: 1.3; text-align: start; }
                p { margin: 0 0 1em; }
                a { color: var(--rb-link); }
                img, video { max-width: 100%; height: auto; border-radius: 6px; }
                figure { margin: 1em 0; }
                figcaption { color: var(--rb-secondary); font-size: 0.85em; text-align: start; }
                blockquote {
                  margin: 1em 0; padding: 0.5em 1em;
                  border-left: 3px solid var(--rb-border);
                  color: var(--rb-secondary);
                }
                pre, code {
                  font-family: 'Roboto Mono', monospace;
                  background: var(--rb-block-bg);
                }
                code { padding: 0.1em 0.3em; border-radius: 4px; }
                pre { padding: 12px; border-radius: 8px; overflow-x: auto; text-align: left; }
                pre code { background: none; padding: 0; }
                table {
                  display: block; width: 100%; overflow-x: auto;
                  border-collapse: collapse;
                }
                th, td { border: 1px solid var(--rb-border); padding: 6px 10px; text-align: start; }
                hr { border: none; border-top: 1px solid var(--rb-border); margin: 1.5em 0; }
              </style>
            </head>
            <body>
              <div class="rb-content">
                $heading
                $contentHtml
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun fontFamily(family: ReaderFontFamily): String = when (family) {
        ReaderFontFamily.Serif -> "Georgia, 'Times New Roman', serif"
        ReaderFontFamily.SansSerif -> "system-ui, 'Roboto', sans-serif"
        ReaderFontFamily.Slab -> "'Roboto Slab', Georgia, serif"
        ReaderFontFamily.Monospace -> "'Roboto Mono', monospace"
        ReaderFontFamily.Dyslexic -> "'OpenDyslexic', 'Comic Sans MS', sans-serif"
    }

    private fun fontWeight(weight: ReaderFontWeight): String = when (weight) {
        ReaderFontWeight.Light -> "300"
        ReaderFontWeight.Normal -> "400"
        ReaderFontWeight.Medium -> "500"
        ReaderFontWeight.Bold -> "700"
    }

    private fun contentWidth(width: ContentWidth): String = when (width) {
        ContentWidth.Narrow -> "32rem"
        ContentWidth.Medium -> "40rem"
        ContentWidth.Wide -> "50rem"
        ContentWidth.Full -> "100%"
    }

    private fun textAlign(align: ReaderTextAlign): String = when (align) {
        ReaderTextAlign.Start -> "start"
        ReaderTextAlign.Justify -> "justify"
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}
