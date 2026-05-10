package com.hackerfit

import com.hackerfit.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FakeRepositoriesTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private lateinit var dailyLogRepo: FakeDailyLogRepository
    private lateinit var streakRepo: FakeStreakRepository
    private lateinit var assessmentRepo: FakeAssessmentRepository

    @Before
    fun setup() {
        profileRepo = FakeUserProfileRepository()
        dailyLogRepo = FakeDailyLogRepository()
        streakRepo = FakeStreakRepository()
        assessmentRepo = FakeAssessmentRepository()
    }

    // UserProfileRepository
    @Test
    fun `profile starts null`() {
        assertNull(profileRepo.profileState.value)
    }

    @Test
    fun `saveProfile sets profile`() = runTest {
        val profile = UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true)
        profileRepo.saveProfile(profile)
        assertEquals(5, profileRepo.profileState.value!!.currentRung)
    }

    @Test
    fun `updateRung changes only rung`() = runTest {
        profileRepo.setProfile(UserProfile(3, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        profileRepo.updateRung(10)
        assertEquals(10, profileRepo.profileState.value!!.currentRung)
        assertEquals(Phase.INTRODUCTORY, profileRepo.profileState.value!!.phase)
    }

    @Test
    fun `updateRung does nothing when null profile`() = runTest {
        profileRepo.updateRung(10)
        assertNull(profileRepo.profileState.value)
    }

    @Test
    fun `completeOnboarding sets flag`() = runTest {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, false))
        profileRepo.completeOnboarding()
        assertTrue(profileRepo.profileState.value!!.onboardingComplete)
    }

    @Test
    fun `setReminderTime updates hours`() = runTest {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        profileRepo.setReminderTime(9, 30)
        assertEquals(9, profileRepo.profileState.value!!.dailyReminderHour)
        assertEquals(30, profileRepo.profileState.value!!.dailyReminderMinute)
    }

    @Test
    fun `clearReminderTime nulls hours`() = runTest {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), 8, 0, true))
        profileRepo.clearReminderTime()
        assertNull(profileRepo.profileState.value!!.dailyReminderHour)
        assertNull(profileRepo.profileState.value!!.dailyReminderMinute)
    }

    // DailyLogRepository
    @Test
    fun `daily logs start empty`() {
        assertTrue(dailyLogRepo.logsState.value.isEmpty())
    }

    @Test
    fun `saveLog adds new log`() = runTest {
        val log = DailyLog(1, LocalDate.now(), 3, true, LocalDate.now())
        dailyLogRepo.saveLog(log)
        assertEquals(1, dailyLogRepo.logsState.value.size)
        assertEquals(3, dailyLogRepo.logsState.value[0].rung)
    }

    @Test
    fun `saveLog replaces existing log by date`() = runTest {
        val today = LocalDate.now()
        dailyLogRepo.saveLog(DailyLog(1, today, 3, false, null))
        dailyLogRepo.saveLog(DailyLog(2, today, 5, true, today))
        assertEquals(1, dailyLogRepo.logsState.value.size)
        assertEquals(5, dailyLogRepo.logsState.value[0].rung)
        assertTrue(dailyLogRepo.logsState.value[0].completed)
    }

    @Test
    fun `hasCompletedToday returns true when completed today`() = runTest {
        dailyLogRepo.saveLog(DailyLog(1, LocalDate.now(), 3, true, LocalDate.now()))
        assertTrue(dailyLogRepo.hasCompletedToday())
    }

    @Test
    fun `hasCompletedToday returns false when not completed`() = runTest {
        dailyLogRepo.saveLog(DailyLog(1, LocalDate.now(), 3, false, null))
        assertFalse(dailyLogRepo.hasCompletedToday())
    }

    @Test
    fun `hasCompletedToday returns false when no logs`() = runTest {
        assertFalse(dailyLogRepo.hasCompletedToday())
    }

    @Test
    fun `hasCompletedToday returns false for yesterday`() = runTest {
        dailyLogRepo.saveLog(DailyLog(1, LocalDate.now().minusDays(1), 3, true, LocalDate.now().minusDays(1)))
        assertFalse(dailyLogRepo.hasCompletedToday())
    }

    @Test
    fun `getLogForDate returns correct log`() = runTest {
        val today = LocalDate.now()
        dailyLogRepo.saveLog(DailyLog(1, today, 5, true, today))
        val log = dailyLogRepo.getLogForDate(today)
        assertNotNull(log)
        assertEquals(5, log!!.rung)
    }

    @Test
    fun `getLogForDate returns null for missing date`() = runTest {
        assertNull(dailyLogRepo.getLogForDate(LocalDate.of(2020, 1, 1)))
    }

    @Test
    fun `getConsecutiveDays counts backward from today`() = runTest {
        val today = LocalDate.now()
        dailyLogRepo.saveLog(DailyLog(1, today, 1, true, today))
        dailyLogRepo.saveLog(DailyLog(2, today.minusDays(1), 1, true, today.minusDays(1)))
        dailyLogRepo.saveLog(DailyLog(3, today.minusDays(2), 1, true, today.minusDays(2)))
        dailyLogRepo.saveLog(DailyLog(4, today.minusDays(4), 1, true, today.minusDays(4)))
        assertEquals(3, dailyLogRepo.getConsecutiveDays())
    }

    @Test
    fun `getConsecutiveDays is 0 when today not completed`() = runTest {
        dailyLogRepo.saveLog(DailyLog(1, LocalDate.now().minusDays(1), 1, true, LocalDate.now().minusDays(1)))
        assertEquals(0, dailyLogRepo.getConsecutiveDays())
    }

    // StreakRepository
    @Test
    fun `streak starts at 0`() {
        assertEquals(0, streakRepo.streakState.value.streakCount)
        assertEquals(0, streakRepo.streakState.value.freezesBanked)
        assertNull(streakRepo.streakState.value.lastFreezeEarnDate)
    }

    @Test
    fun `incrementStreak increases count by 1`() = runTest {
        streakRepo.incrementStreak()
        assertEquals(1, streakRepo.streakState.value.streakCount)
    }

    @Test
    fun `incrementStreak multiple times`() = runTest {
        repeat(3) { streakRepo.incrementStreak() }
        assertEquals(3, streakRepo.streakState.value.streakCount)
    }

    @Test
    fun `incrementStreak earns freeze every 5 days`() = runTest {
        repeat(5) { streakRepo.incrementStreak() }
        assertEquals(5, streakRepo.streakState.value.streakCount)
        assertEquals(1, streakRepo.streakState.value.freezesBanked)
    }

    @Test
    fun `incrementStreak earns second freeze at 10 days`() = runTest {
        repeat(10) { streakRepo.incrementStreak() }
        assertEquals(10, streakRepo.streakState.value.streakCount)
        assertEquals(2, streakRepo.streakState.value.freezesBanked)
    }

    @Test
    fun `incrementStreak max 5 freezes`() = runTest {
        repeat(30) { streakRepo.incrementStreak() }
        assertEquals(5, streakRepo.streakState.value.freezesBanked)
    }

    @Test
    fun `resetStreak sets count to 0`() = runTest {
        repeat(5) { streakRepo.incrementStreak() }
        streakRepo.resetStreak()
        assertEquals(0, streakRepo.streakState.value.streakCount)
    }

    @Test
    fun `useFreeze decreases freezes`() = runTest {
        streakRepo.setStreakData(StreakData(10, 2, null))
        streakRepo.useFreeze()
        assertEquals(1, streakRepo.streakState.value.freezesBanked)
    }

    @Test
    fun `useFreeze does nothing when 0 freezes`() = runTest {
        streakRepo.useFreeze()
        assertEquals(0, streakRepo.streakState.value.freezesBanked)
    }

    @Test
    fun `incrementStreak sets lastFreezeEarnDate when freeze earned`() = runTest {
        repeat(5) { streakRepo.incrementStreak() }
        assertNotNull(streakRepo.streakState.value.lastFreezeEarnDate)
        assertEquals(LocalDate.now(), streakRepo.streakState.value.lastFreezeEarnDate)
    }

    // AssessmentRepository
    @Test
    fun `assessments start empty`() {
        assertTrue(assessmentRepo.assessmentsState.value.isEmpty())
    }

    @Test
    fun `saveAssessment adds to list`() = runTest {
        assessmentRepo.saveAssessment(AssessmentLog(1, LocalDate.now(), 3, 4, true, null))
        assertEquals(1, assessmentRepo.assessmentsState.value.size)
    }

    @Test
    fun `saveAssessment accumulates`() = runTest {
        assessmentRepo.saveAssessment(AssessmentLog(1, LocalDate.now().minusDays(1), 3, 4, true, null))
        assessmentRepo.saveAssessment(AssessmentLog(2, LocalDate.now(), 4, 5, false, null))
        assertEquals(2, assessmentRepo.assessmentsState.value.size)
    }

    @Test
    fun `getLastAssessmentDate returns latest date`() = runTest {
        assessmentRepo.saveAssessment(AssessmentLog(1, LocalDate.of(2025, 1, 1), 1, 2, true, null))
        assessmentRepo.saveAssessment(AssessmentLog(2, LocalDate.of(2025, 3, 15), 2, 3, true, null))
        assertEquals(LocalDate.of(2025, 3, 15), assessmentRepo.getLastAssessmentDate())
    }

    @Test
    fun `getLastAssessmentDate returns null when empty`() = runTest {
        assertNull(assessmentRepo.getLastAssessmentDate())
    }
}
