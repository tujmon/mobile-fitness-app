package com.hackerfit.data.local.db

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromLocalDate converts to ISO string`() {
        val date = LocalDate.of(2025, 3, 15)
        assertEquals("2025-03-15", converters.fromLocalDate(date))
    }

    @Test
    fun `fromLocalDate returns null for null input`() {
        assertNull(converters.fromLocalDate(null))
    }

    @Test
    fun `toLocalDate parses ISO string`() {
        val date = converters.toLocalDate("2025-03-15")
        assertEquals(LocalDate.of(2025, 3, 15), date)
    }

    @Test
    fun `toLocalDate returns null for null input`() {
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun `roundtrip preserves date`() {
        val original = LocalDate.of(2024, 12, 25)
        val string = converters.fromLocalDate(original)
        val parsed = converters.toLocalDate(string)
        assertEquals(original, parsed)
    }

    @Test
    fun `roundtrip for edge date - year start`() {
        val date = LocalDate.of(2025, 1, 1)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun `roundtrip for edge date - year end`() {
        val date = LocalDate.of(2025, 12, 31)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun `roundtrip for leap year date`() {
        val date = LocalDate.of(2024, 2, 29)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun `handles epoch date`() {
        val date = LocalDate.of(1970, 1, 1)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }

    @Test
    fun `handles far future date`() {
        val date = LocalDate.of(2099, 6, 15)
        assertEquals(date, converters.toLocalDate(converters.fromLocalDate(date)))
    }
}
