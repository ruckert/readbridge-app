package com.readbridge.app.domain.reader.model

/**
 * User-tunable reading experience (see PLAN §6-A). Applied live in the reader's WebView
 * via CSS variables and persisted globally so every article opens with the same settings.
 */
data class ReadingPreferences(
    val theme: ReadingTheme = ReadingTheme.System,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.Serif,
    val fontSizeSp: Int = DEFAULT_FONT_SIZE,
    val fontWeight: ReaderFontWeight = ReaderFontWeight.Normal,
    val lineHeight: Float = DEFAULT_LINE_HEIGHT,
    val letterSpacingEm: Float = DEFAULT_LETTER_SPACING,
    val contentWidth: ContentWidth = ContentWidth.Medium,
    val textAlign: ReaderTextAlign = ReaderTextAlign.Start,
    val hyphenation: Boolean = false,
    val customBackgroundHex: String = DEFAULT_CUSTOM_BACKGROUND,
    val customTextHex: String = DEFAULT_CUSTOM_TEXT,
) {
    companion object {
        const val DEFAULT_FONT_SIZE = 18
        const val MIN_FONT_SIZE = 12
        const val MAX_FONT_SIZE = 32

        const val DEFAULT_LINE_HEIGHT = 1.6f
        const val MIN_LINE_HEIGHT = 1.2f
        const val MAX_LINE_HEIGHT = 2.2f

        const val DEFAULT_LETTER_SPACING = 0f
        const val MIN_LETTER_SPACING = -0.02f
        const val MAX_LETTER_SPACING = 0.08f

        const val DEFAULT_CUSTOM_BACKGROUND = "#FFFFFF"
        const val DEFAULT_CUSTOM_TEXT = "#1B1B1B"
    }
}

enum class ReadingTheme { Light, Sepia, Gray, Oled, System, Custom }

enum class ReaderFontFamily { Serif, SansSerif, Slab, Monospace, Dyslexic }

enum class ReaderFontWeight { Light, Normal, Medium, Bold }

enum class ContentWidth { Narrow, Medium, Wide, Full }

enum class ReaderTextAlign { Start, Justify }
