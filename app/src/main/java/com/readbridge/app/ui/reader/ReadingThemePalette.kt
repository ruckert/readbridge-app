package com.readbridge.app.ui.reader

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

/** Resolve a [ReadingTheme] to concrete colors; [systemDark] decides the System theme. */
fun resolveReadingPalette(theme: ReadingTheme, systemDark: Boolean): ReadingThemePalette =
    when (theme) {
        ReadingTheme.Light -> Light
        ReadingTheme.Sepia -> Sepia
        ReadingTheme.Gray -> Gray
        ReadingTheme.Oled -> Oled
        ReadingTheme.System -> if (systemDark) Gray else Light
    }
