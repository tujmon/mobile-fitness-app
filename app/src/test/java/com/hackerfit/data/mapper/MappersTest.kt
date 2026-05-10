package com.hackerfit.data.mapper

import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MappersTest {

    @Test
    fun `UserProfileEntity toDomain maps introductory phase`() {
        val entity = UserProfileEntity(
            id = 1,
            currentRung = 5,
            phase = "introductory",
            rungStartDate = LocalDate.of(2025, 1, 1),
            dailyReminderHour = 8,
            dailyReminderMinute = 30,
            onboardingComplete = true
        )
        val domain = entity.toDomain()
        assertEquals(5, domain.currentRung)
        assertEquals(Phase.INTRODUCTORY, domain.phase)
        assertEquals(LocalDate.of(2025, 1, 1), domain.rungStartDate)
        assertEquals(8, domain.dailyReminderHour)
        assertEquals(30, domain.dailyReminderMinute)
        assertTrue(domain.onboardingComplete)
    }

    @Test
    fun `UserProfileEntity toDomain maps lifetime phase`() {
        val entity = UserProfileEntity(
            phase = "lifetime",
            currentRung = 20
        )
        val domain = entity.toDomain()
        assertEquals(Phase.LIFETIME, domain.phase)
        assertEquals(20, domain.currentRung)
    }

    @Test
    fun `UserProfileEntity toDomain maps unknown phase as lifetime`() {
        val entity = UserProfileEntity(phase = "something_else", currentRung = 1)
        val domain = entity.toDomain()
        assertEquals(Phase.LIFETIME, domain.phase)
    }

    @Test
    fun `UserProfileEntity toDomain maps null reminders`() {
        val entity = UserProfileEntity(
            dailyReminderHour = null,
            dailyReminderMinute = null
        )
        val domain = entity.toDomain()
        assertNull(domain.dailyReminderHour)
        assertNull(domain.dailyReminderMinute)
    }

    @Test
    fun `UserProfileEntity toDomain maps onboardingComplete false`() {
        val entity = UserProfileEntity(onboardingComplete = false)
        assertFalse(entity.toDomain().onboardingComplete)
    }

    @Test
    fun `DailyLogEntity toDomain maps all fields`() {
        val date = LocalDate.of(2025, 3, 15)
        val entity = DailyLogEntity(
            id = 42,
            date = date,
            rung = 10,
            completed = true,
            completedAt = date
        )
        val domain = entity.toDomain()
        assertEquals(42L, domain.id)
        assertEquals(date, domain.date)
        assertEquals(10, domain.rung)
        assertTrue(domain.completed)
        assertEquals(date, domain.completedAt)
    }

    @Test
    fun `DailyLogEntity toDomain maps incomplete log`() {
        val entity = DailyLogEntity(
            id = 1,
            date = LocalDate.now(),
            rung = 5,
            completed = false,
            completedAt = null
        )
        val domain = entity.toDomain()
        assertFalse(domain.completed)
        assertNull(domain.completedAt)
    }

    @Test
    fun `DailyLogEntity toDomain uses default id`() {
        val entity = DailyLogEntity(
            date = LocalDate.now(),
            rung = 1,
            completed = true,
            completedAt = LocalDate.now()
        )
        assertEquals(0L, entity.toDomain().id)
    }

    @Test
    fun `AssessmentLogEntity toDomain maps all fields`() {
        val entity = AssessmentLogEntity(
            id = 10,
            date = LocalDate.of(2025, 2, 1),
            fromRung = 5,
            toRung = 6,
            passed = true,
            notes = "Easy"
        )
        val domain = entity.toDomain()
        assertEquals(10L, domain.id)
        assertEquals(LocalDate.of(2025, 2, 1), domain.date)
        assertEquals(5, domain.fromRung)
        assertEquals(6, domain.toRung)
        assertTrue(domain.passed)
        assertEquals("Easy", domain.notes)
    }

    @Test
    fun `AssessmentLogEntity toDomain maps failed assessment`() {
        val entity = AssessmentLogEntity(
            date = LocalDate.now(),
            fromRung = 10,
            toRung = 11,
            passed = false,
            notes = null
        )
        val domain = entity.toDomain()
        assertFalse(domain.passed)
        assertNull(domain.notes)
    }

    @Test
    fun `AssessmentLogEntity toDomain uses default id`() {
        val entity = AssessmentLogEntity(
            date = LocalDate.now(),
            fromRung = 1,
            toRung = 2,
            passed = true
        )
        assertEquals(0L, entity.toDomain().id)
    }
}
