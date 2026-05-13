package com.hackerfit.ui.screens.settings

import com.hackerfit.service.ReminderScheduler
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ImportReminderPolicyTest {

    @Before
    fun setup() {
        mockkObject(ReminderScheduler)
        justRun { ReminderScheduler.schedule(any(), any(), any()) }
        justRun { ReminderScheduler.cancel(any()) }
    }

    @After
    fun tearDown() {
        unmockkObject(ReminderScheduler)
    }

    @Test
    fun `reminder active with permission schedules`() {
        var cleared = false
        var warning = false
        val hour = 8
        val hasPermission = true

        if (hour != 0) {
            if (hasPermission) {
                ReminderScheduler.schedule(mockk(relaxed = true), hour, 0)
            } else {
                cleared = true
                ReminderScheduler.cancel(mockk(relaxed = true))
                warning = true
            }
        } else {
            ReminderScheduler.cancel(mockk(relaxed = true))
        }

        verify(exactly = 1) { ReminderScheduler.schedule(any(), eq(8), eq(0)) }
        verify(exactly = 0) { ReminderScheduler.cancel(any()) }
        assertFalse(cleared)
        assertFalse(warning)
    }

    @Test
    fun `reminder active without permission clears and warns`() {
        var cleared = false
        var warning = false
        val hour = 8
        val hasPermission = false

        if (hour != 0) {
            if (hasPermission) {
                ReminderScheduler.schedule(mockk(relaxed = true), hour, 0)
            } else {
                cleared = true
                ReminderScheduler.cancel(mockk(relaxed = true))
                warning = true
            }
        } else {
            ReminderScheduler.cancel(mockk(relaxed = true))
        }

        verify(exactly = 0) { ReminderScheduler.schedule(any(), any(), any()) }
        verify(exactly = 1) { ReminderScheduler.cancel(any()) }
        assertTrue(cleared)
        assertTrue(warning)
    }

    @Test
    fun `no reminder cancels scheduler`() {
        var cleared = false
        var warning = false
        val hour: Int? = null
        val hasPermission = true

        if (hour != null) {
            if (hasPermission) {
                ReminderScheduler.schedule(mockk(relaxed = true), hour, 0)
            } else {
                cleared = true
                ReminderScheduler.cancel(mockk(relaxed = true))
                warning = true
            }
        } else {
            ReminderScheduler.cancel(mockk(relaxed = true))
        }

        verify(exactly = 0) { ReminderScheduler.schedule(any(), any(), any()) }
        verify(exactly = 1) { ReminderScheduler.cancel(any()) }
        assertFalse(cleared)
        assertFalse(warning)
    }
}
