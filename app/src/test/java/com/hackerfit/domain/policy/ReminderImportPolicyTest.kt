package com.hackerfit.domain.policy

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ReminderImportPolicyTest {

    private lateinit var policy: ReminderImportPolicy

    @Before
    fun setup() {
        policy = ReminderImportPolicy()
    }

    @Test
    fun `reminder active with permission returns Schedule`() {
        val decision = policy.resolve(
            reminderHour = 8,
            reminderMinute = 30,
            hasNotificationPermission = true
        )
        assertTrue(decision is ReminderImportDecision.Schedule)
        val schedule = decision as ReminderImportDecision.Schedule
        assertEquals(8, schedule.hour)
        assertEquals(30, schedule.minute)
    }

    @Test
    fun `reminder active with permission defaults minute to 0`() {
        val decision = policy.resolve(
            reminderHour = 9,
            reminderMinute = null,
            hasNotificationPermission = true
        )
        assertTrue(decision is ReminderImportDecision.Schedule)
        val schedule = decision as ReminderImportDecision.Schedule
        assertEquals(9, schedule.hour)
        assertEquals(0, schedule.minute)
    }

    @Test
    fun `reminder active without permission returns ClearAndCancel`() {
        val decision = policy.resolve(
            reminderHour = 8,
            reminderMinute = 30,
            hasNotificationPermission = false
        )
        assertTrue(decision is ReminderImportDecision.ClearAndCancel)
        val clear = decision as ReminderImportDecision.ClearAndCancel
        assertTrue(clear.warningMessage.contains("lembrete"))
    }

    @Test
    fun `no reminder returns CancelOnly`() {
        val decision = policy.resolve(
            reminderHour = null,
            reminderMinute = null,
            hasNotificationPermission = true
        )
        assertTrue(decision is ReminderImportDecision.CancelOnly)
    }

    @Test
    fun `no reminder ignores permission`() {
        val decision = policy.resolve(
            reminderHour = null,
            reminderMinute = null,
            hasNotificationPermission = false
        )
        assertTrue(decision is ReminderImportDecision.CancelOnly)
    }
}
