package com.readbridge.app.ui.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun `parses shorthand and full hex`() {
        assertEquals(Triple(255, 255, 255), ColorUtils.parse("#fff"))
        assertEquals(Triple(27, 27, 27), ColorUtils.parse("#1B1B1B"))
        assertEquals(Triple(0, 0, 0), ColorUtils.parse("000000"))
    }

    @Test
    fun `rejects invalid hex`() {
        assertNull(ColorUtils.parse("#12"))
        assertNull(ColorUtils.parse("#GGGGGG"))
        assertNull(ColorUtils.parse(""))
    }

    @Test
    fun `sanitize normalizes or falls back`() {
        assertEquals("#FFFFFF", ColorUtils.sanitize("#fff", "#000000"))
        assertEquals("#000000", ColorUtils.sanitize("nope", "#000000"))
    }

    @Test
    fun `black on white is maximum contrast`() {
        assertEquals(21.0, ColorUtils.contrastRatio("#000000", "#FFFFFF"), 0.1)
    }

    @Test
    fun `contrast drives AA decision`() {
        assertTrue(ColorUtils.meetsAa(background = "#FFFFFF", text = "#1B1B1B"))
        assertFalse(ColorUtils.meetsAa(background = "#FFFFFF", text = "#DDDDDD"))
    }

    @Test
    fun `blend endpoints and midpoint`() {
        assertEquals("#000000", ColorUtils.blend("#000000", "#FFFFFF", 0.0))
        assertEquals("#FFFFFF", ColorUtils.blend("#000000", "#FFFFFF", 1.0))
        assertEquals("#7F7F7F", ColorUtils.blend("#000000", "#FFFFFF", 0.5))
    }
}
