package com.hackerfit.ui.screens.history

import com.hackerfit.FakeAssessmentRepository
import com.hackerfit.FakeDailyLogRepository
import com.hackerfit.FakeStreakRepository
import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.DailyLog
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class HistoryViewModelTest {

    private lateinit var dailyLogRepo: FakeDailyLogRepository
    private lateinit var assessmentRepo: FakeAssessmentRepository
    private lateinit var streakRepo: FakeStreakRepository
    private lateinit var userProfileRepo: FakeUserProfileRepository
    private lateinit var viewModel: HistoryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        dailyLogRepo = FakeDailyLogRepository()
        assessmentRepo = FakeAssessmentRepository()
        streakRepo = FakeStreakRepository()
        userProfileRepo = FakeUserProfileRepository()
    }

    private fun createViewModel() {
        viewModel = HistoryViewModel(dailyLogRepo, assessmentRepo, streakRepo, userProfileRepo)
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        createViewModel()
        assertTrue(viewModel.uiState.value is HistoryUiState.Loading)
    }

    @Test
    fun `shows completed dates from logs`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 3, true, today),
            DailyLog(2, yesterday, 3, true, yesterday),
            DailyLog(3, today.minusDays(3), 3, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(2, state.completedDates.size)
        assertTrue(state.completedDates.contains(today))
        assertTrue(state.completedDates.contains(yesterday))
    }

    @Test
    fun `selected month defaults to current month`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(YearMonth.now(), state.selectedMonth)
    }

    @Test
    fun `selectMonth updates selected month`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val target = YearMonth.of(2025, 3)
        viewModel.selectMonth(target)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(target, state.selectedMonth)
    }

    @Test
    fun `selectDay updates selected day and detail`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 5, true, today)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDay(today)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(today, state.selectedDay)
        assertNotNull(state.dayDetail)
        assertEquals(5, state.dayDetail!!.rung)
        assertTrue(state.dayDetail!!.completed)
    }

    @Test
    fun `selectDay with null clears selection`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        dailyLogRepo.setLogs(listOf(DailyLog(1, today, 3, true, today)))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDay(today)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDay(null)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertNull(state.selectedDay)
        assertNull(state.dayDetail)
    }

    @Test
    fun `selectDay with no log shows null detail`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDay(LocalDate.now().plusDays(10))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertNotNull(state.selectedDay)
        assertNull(state.dayDetail)
    }

    @Test
    fun `recentLogs filtered to selected month`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 3, true, today),
            DailyLog(2, today.minusDays(5), 3, true, today.minusDays(5)),
            DailyLog(3, lastMonth, 2, true, lastMonth)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(YearMonth.now(), state.selectedMonth)
        val monthLogs = state.recentLogs
        for (log in monthLogs) {
            assertEquals(YearMonth.from(log.date), YearMonth.now())
        }
    }

    @Test
    fun `assessments shown in state`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        assessmentRepo.setAssessments(listOf(
            com.hackerfit.domain.model.AssessmentLog(1, LocalDate.now().minusDays(5), 3, 4, true, null),
            com.hackerfit.domain.model.AssessmentLog(2, LocalDate.now().minusDays(2), 4, 5, false, null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(2, state.assessments.size)
        assertEquals(3, state.assessments[0].fromRung)
        assertFalse(state.assessments[1].passed)
    }

    @Test
    fun `selecting different month filters logs`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 3, true, today),
            DailyLog(2, lastMonth.plusDays(5), 2, true, lastMonth.plusDays(5)),
            DailyLog(3, lastMonth.plusDays(10), 2, true, lastMonth.plusDays(10))
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectMonth(YearMonth.from(lastMonth))
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(YearMonth.from(lastMonth), state.selectedMonth)
        assertEquals(2, state.recentLogs.size)
    }

    @Test
    fun `selectDay then change month preserves day if in new month`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 3, true, today)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectDay(today)
        testDispatcher.scheduler.advanceUntilIdle()
        val state1 = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(today, state1.selectedDay)
    }

    @Test
    fun `logByDate contains all logs`() = runTest(testDispatcher) {
        val d1 = LocalDate.now().minusDays(2)
        val d2 = LocalDate.now().minusDays(1)
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, d1, 3, true, d1),
            DailyLog(2, d2, 5, true, d2)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(2, state.logByDate.size)
        assertTrue(state.logByDate.containsKey(d1))
        assertTrue(state.logByDate.containsKey(d2))
        assertEquals(3, state.logByDate[d1]!!.rung)
        assertEquals(5, state.logByDate[d2]!!.rung)
    }

    @Test
    fun `completed dates excludes incomplete logs`() = runTest(testDispatcher) {
        val today = LocalDate.now()
        dailyLogRepo.setLogs(listOf(
            DailyLog(1, today, 3, false, null),
            DailyLog(2, today.minusDays(1), 3, true, today.minusDays(1))
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(1, state.completedDates.size)
        assertFalse(state.completedDates.contains(today))
        assertTrue(state.completedDates.contains(today.minusDays(1)))
    }

    @Test
    fun `selecting future day with no log shows null detail`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val futureDate = LocalDate.now().plusMonths(1)
        viewModel.selectDay(futureDate)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HistoryUiState.Success
        assertEquals(futureDate, state.selectedDay)
        assertNull(state.dayDetail)
    }

    @Test
    fun `multiple month selections work`() = runTest(testDispatcher) {
        dailyLogRepo.setLogs(emptyList())
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectMonth(YearMonth.of(2024, 6))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(YearMonth.of(2024, 6), (viewModel.uiState.value as HistoryUiState.Success).selectedMonth)

        viewModel.selectMonth(YearMonth.of(2025, 1))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(YearMonth.of(2025, 1), (viewModel.uiState.value as HistoryUiState.Success).selectedMonth)

        viewModel.selectMonth(YearMonth.now())
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(YearMonth.now(), (viewModel.uiState.value as HistoryUiState.Success).selectedMonth)
    }
}
