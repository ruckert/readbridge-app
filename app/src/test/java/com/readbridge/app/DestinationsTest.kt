package com.readbridge.app

import com.readbridge.app.ui.navigation.Destinations
import org.junit.Assert.assertEquals
import org.junit.Test

class DestinationsTest {
    @Test
    fun reader_route_interpolates_entry_id() {
        assertEquals("reader/42", Destinations.reader(42L))
    }
}
