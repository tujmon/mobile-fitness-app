package com.hackerfit.ui.screens.home

import app.cash.turbine.test
import com.hackerfit.FakeDailyLogRepository
import com.hackerfit.FakeStreakRepository
import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.DailyLog
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.StreakData
import com.hackerfit.domain.model.UserProfile
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HomeViewModelTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private lateinit var dailyLogRepo: FakeDailyLogRepository
    private lateinit var streakRepo: FakeStreakRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testProfile = UserProfile(
        currentRung = 3,
        phase = Phase.INTRODUCTORY,
        rungStartDate = LocalDate.now().minusDays(2),
        dailyReminderHour = null,
        dailyReminderMinute = null,
        onboardingComplete = true
    )

    @Before
    fun setup() {
        profileRepo = FakeUserProfileRepository()
        dailyLogRepo = FakeDailyLogRepository()
        streakRepo = FakeStreakRepository()
    }

    private fun createViewModel() {
        viewModel = HomeViewModel(profileRepo, dailyLogRepo, streakRepo)
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        createViewModel()
        assertTrue(viewModel.uiState.value is HomeUiState.Loading)
    }

    @Test
    fun `when profile is null shows NotOnboarded`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is HomeUiState.NotOnboarded)
    }

    @Test
    fun `when profile exists shows Success with correct rung`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(5, 1, null))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(3, state.currentRung)
    }

    @Test
    fun `shows correct streak count`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(7, 1, LocalDate.now()))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(7, state.streakCount)
    }

    @Test
    fun `shows freezes banked`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(10, 2, LocalDate.now()))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(2, state.freezesBanked)
    }

    @Test
    fun `completedToday is false when no log today`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(0, 0, null))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertFalse(state.completedToday)
    }

    @Test
    fun `completedToday is true when log exists`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(date = LocalDate.now(), rung = 3, completed = true, completedAt = LocalDate.now())
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertTrue(state.completedToday)
    }

    @Test
    fun `creates default profile when none exists`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val profile = profileRepo.profileState.value
        assertNotNull(profile)
        assertEquals(1, profile!!.currentRung)
        assertFalse(profile.onboardingComplete)
    }

    @Test
    fun `does not overwrite existing profile`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val profile = profileRepo.profileState.value
        assertEquals(3, profile!!.currentRung)
    }

    @Test
    fun `rungStartDate is correct`() = runTest(testDispatcher) {
        val startDate = LocalDate.now().minusDays(5)
        profileRepo.setProfile(testProfile.copy(rungStartDate = startDate))
        streakRepo.setStreakData(StreakData(0, 0, null))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(startDate, state.rungStartDate)
    }

    @Test
    fun `completedToday with incomplete log is false`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(date = LocalDate.now(), rung = 3, completed = false, completedAt = null)
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertFalse(state.completedToday)
    }

    @Test
    fun `handles lifetime rung profile`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(25, Phase.LIFETIME, LocalDate.now().minusDays(10), null, null, true))
        streakRepo.setStreakData(StreakData(5, 1, null))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(25, state.currentRung)
    }

    @Test
    fun `streak updates when repo changes`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(3, 0, null))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, (viewModel.uiState.value as HomeUiState.Success).streakCount)
        streakRepo.setStreakData(StreakData(4, 0, null))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(4, (viewModel.uiState.value as HomeUiState.Success).streakCount)
    }

    @Test
    fun `default profile has correct default values`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val profile = profileRepo.profileState.value!!
        assertEquals(1, profile.currentRung)
        assertEquals(Phase.INTRODUCTORY, profile.phase)
        assertEquals(LocalDate.now(), profile.rungStartDate)
        assertEquals(8, profile.dailyReminderHour)
        assertEquals(0, profile.dailyReminderMinute)
        assertFalse(profile.onboardingComplete)
    }

    @Test
    fun `completedToday with yesterday log is false`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile)
        streakRepo.setStreakData(StreakData(0, 0, null))
        dailyLogRepo.setLogs(listOf(
            DailyLog(date = LocalDate.now().minusDays(1), rung = 3, completed = true, completedAt = LocalDate.now().minusDays(1))
        ))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as HomeUiState.Success
        assertFalse(state.completedToday)
    }
}
