package com.readbridge.app.ui.reader

import com.readbridge.app.domain.reader.model.ReadingPreferences
import com.readbridge.app.domain.reader.model.ReadingTheme

/** Resolved colors (CSS hex) for a reading theme. */
data class ReadingThemePalette(
    val background: String,
    val text: String,
    val secondary: String,
    val link: String,
    val blockBackground: String,
    val border: String,
    val isDark: Boolean,
)

private val Light = ReadingThemePalette(
    background = "#FFFFFF", text = "#1B1B1B", secondary = "#6B6B6B",
    link = "#1565C0", blockBackground = "#F3F3F3", border = "#E4E4E4", isDark = false,
)
private val Sepia = ReadingThemePalette(
    background = "#F5ECD9", text = "#5B4636", secondary = "#857254",
    link = "#9A6A3A", blockBackground = "#EADFC7", border = "#DCCBA6", isDark = false,
)
private val Gray = ReadingThemePalette(
    background = "#22242A", text = "#D7D9DE", secondary = "#9BA0A8",
    link = "#90CAF9", blockBackground = "#2C2F36", border = "#3A3E46", isDark = true,
)
private val Oled = ReadingThemePalette(
    background = "#000000", text = "#C9CDD3", secondary = "#8A9099",
    link = "#90CAF9", blockBackground = "#101216", border = "#23262C", isDark = true,
)

/** Resolve preferences to concrete colors; [systemDark] decides System, custom colors decide Custom. */
fun resolveReadingPalette(prefs: ReadingPreferences, systemDark: Boolean): ReadingThemePalette =
    when (prefs.theme) {
        ReadingTheme.Light -> Light
        ReadingTheme.Sepia -> Sepia
        ReadingTheme.Gray -> Gray
        ReadingTheme.Oled -> Oled
        ReadingTheme.System -> if (systemDark) Gray else Light
        ReadingTheme.Custom -> customPalette(prefs)
    }

private fun customPalette(prefs: ReadingPreferences): ReadingThemePalette {
    val bg = ColorUtils.sanitize(prefs.customBackgroundHex, ReadingPreferences.DEFAULT_CUSTOM_BACKGROUND)
    val text = ColorUtils.sanitize(prefs.customTextHex, ReadingPreferences.DEFAULT_CUSTOM_TEXT)
    val dark = ColorUtils.relativeLuminance(bg) < 0.5
    return ReadingThemePalette(
        background = bg,
        text = text,
        secondary = ColorUtils.blend(text, bg, 0.35),
        link = if (dark) "#90CAF9" else "#1565C0",
        blockBackground = ColorUtils.blend(bg, text, 0.06),
        border = ColorUtils.blend(bg, text, 0.18),
        isDark = dark,
    )
}
