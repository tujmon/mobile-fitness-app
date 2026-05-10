package com.hackerfit.ui.screens.stats

import com.hackerfit.FakeAssessmentRepository
import com.hackerfit.FakeDailyLogRepository
import com.hackerfit.FakeStreakRepository
import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StatsViewModelTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private lateinit var dailyLogRepo: FakeDailyLogRepository
    private lateinit var assessmentRepo: FakeAssessmentRepository
    private lateinit var streakRepo: FakeStreakRepository
    private lateinit var viewModel: StatsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        profileRepo = FakeUserProfileRepository()
        dailyLogRepo = FakeDailyLogRepository()
        assessmentRepo = FakeAssessmentRepository()
        streakRepo = FakeStreakRepository()
    }

    private fun createViewModel() {
        viewModel = StatsViewModel(dailyLogRepo, assessmentRepo, streakRepo, profileRepo)
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        createViewModel()
        assertTrue(viewModel.uiState.value is StatsUiState.Loading)
    }

    @Test
    fun `shows total workouts from completed logs`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(3, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, LocalDate.now().minusDays(2), 5, true, LocalDate.now().minusDays(2)),
            DailyLog(2, LocalDate.now().minusDays(1), 5, true, LocalDate.now().minusDays(1)),
            DailyLog(3, LocalDate.now(), 5, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(2, state.totalWorkouts)
    }

    @Test
    fun `shows current streak from streak repo`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(3, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(7, 1, LocalDate.now()))
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(7, state.currentStreak)
    }

    @Test
    fun `calculates best streak from consecutive completed dates`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, LocalDate.now().minusDays(6), 1, true, LocalDate.now().minusDays(6)),
            DailyLog(2, LocalDate.now().minusDays(5), 1, true, LocalDate.now().minusDays(5)),
            DailyLog(3, LocalDate.now().minusDays(4), 1, true, LocalDate.now().minusDays(4)),
            DailyLog(4, LocalDate.now().minusDays(2), 1, true, LocalDate.now().minusDays(2)),
            DailyLog(5, LocalDate.now().minusDays(1), 1, true, LocalDate.now().minusDays(1))
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(3, state.bestStreak)
    }

    @Test
    fun `shows current rung from profile`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(12, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(12, state.currentRung)
    }

    @Test
    fun `weekly data has 7 entries`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(7, state.weeklyData.size)
    }

    @Test
    fun `rung progression shows passed assessments`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(emptyList())
        assessmentRepo.setAssessments(listOf(
            AssessmentLog(1, LocalDate.now().minusDays(10), 3, 4, true, null),
            AssessmentLog(2, LocalDate.now().minusDays(5), 4, 5, true, null),
            AssessmentLog(3, LocalDate.now().minusDays(3), 5, 6, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(2, state.rungProgression.size)
        assertEquals(3, state.rungProgression[0].fromRung)
        assertEquals(4, state.rungProgression[1].fromRung)
    }

    @Test
    fun `best streak is 0 when no completed logs`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(0, state.bestStreak)
    }

    @Test
    fun `totalWorkouts ignores incomplete logs`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, LocalDate.now().minusDays(1), 1, false, null),
            DailyLog(2, LocalDate.now().minusDays(2), 1, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(0, state.totalWorkouts)
    }

    @Test
    fun `best streak handles single day`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, LocalDate.now(), 1, true, LocalDate.now())
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(1, state.bestStreak)
    }

    @Test
    fun `best streak handles gap then new streak`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, LocalDate.now().minusDays(5), 1, true, LocalDate.now().minusDays(5)),
            DailyLog(2, LocalDate.now().minusDays(4), 1, true, LocalDate.now().minusDays(4)),
            DailyLog(3, LocalDate.now().minusDays(1), 1, true, LocalDate.now().minusDays(1)),
            DailyLog(4, LocalDate.now(), 1, true, LocalDate.now())
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(2, state.bestStreak)
    }

    @Test
    fun `weekly data marks completed days`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 1, true, today)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        val todayEntry = state.weeklyData.last()
        assertTrue(todayEntry.completed)
        assertEquals(1, todayEntry.rung)
        val yesterdayEntry = state.weeklyData[state.weeklyData.size - 2]
        assertFalse(yesterdayEntry.completed)
    }

    @Test
    fun `rung progression only shows passed assessments`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(emptyList())
        assessmentRepo.setAssessments(listOf(
            AssessmentLog(1, LocalDate.now(), 1, 2, false, null),
            AssessmentLog(2, LocalDate.now(), 2, 3, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(0, state.rungProgression.size)
    }

    @Test
    fun `defaults to rung 1 when no profile`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as StatsUiState.Success
        assertEquals(1, state.currentRung)
    }
}
