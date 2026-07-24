package com.readbridge.app.ui.reader

import kotlin.math.pow

/** Pure color helpers for the custom reading theme (hex parsing, WCAG contrast, blending). */
object ColorUtils {

    /** Parse `#RGB` or `#RRGGBB` into an (r, g, b) triple of 0..255, or null if invalid. */
    fun parse(hex: String): Triple<Int, Int, Int>? {
        val cleaned = hex.trim().removePrefix("#")
        val full = when (cleaned.length) {
            3 -> cleaned.map { "$it$it" }.joinToString("")
            6 -> cleaned
            else -> return null
        }
        if (!full.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        return Triple(
            full.substring(0, 2).toInt(16),
            full.substring(2, 4).toInt(16),
            full.substring(4, 6).toInt(16),
        )
    }

    fun isValid(hex: String): Boolean = parse(hex) != null

    /** Return a normalized `#RRGGBB`, or [default] if [hex] is invalid. */
    fun sanitize(hex: String, default: String): String {
        val rgb = parse(hex) ?: return default
        return "#%02X%02X%02X".format(rgb.first, rgb.second, rgb.third)
    }

    /** WCAG relative luminance (0f dark .. 1f light). Invalid input is treated as white. */
    fun relativeLuminance(hex: String): Double {
        val (r, g, b) = parse(hex) ?: return 1.0
        fun channel(c: Int): Double {
            val s = c / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
    }

    /** WCAG contrast ratio between two colors (1.0 .. 21.0). */
    fun contrastRatio(hex1: String, hex2: String): Double {
        val l1 = relativeLuminance(hex1)
        val l2 = relativeLuminance(hex2)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** WCAG AA for body text requires a contrast ratio of at least 4.5:1. */
    fun meetsAa(background: String, text: String): Boolean =
        contrastRatio(background, text) >= 4.5

    /** Linear blend from [from] (t=0) toward [to] (t=1). Falls back to [from] on invalid input. */
    fun blend(from: String, to: String, t: Double): String {
        val a = parse(from) ?: return from
        val b = parse(to) ?: return from
        val ratio = t.coerceIn(0.0, 1.0)
        fun mix(x: Int, y: Int) = (x + (y - x) * ratio).toInt().coerceIn(0, 255)
        return "#%02X%02X%02X".format(
            mix(a.first, b.first),
            mix(a.second, b.second),
            mix(a.third, b.third),
        )
    }
}
