package com.hackerfit.ui.screens.workout

import app.cash.turbine.test
import com.hackerfit.FakeDailyLogRepository
import com.hackerfit.FakeStreakRepository
import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.UserProfile
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WorkoutViewModelTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private lateinit var dailyLogRepo: FakeDailyLogRepository
    private lateinit var streakRepo: FakeStreakRepository
    private lateinit var viewModel: WorkoutViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testProfile = UserProfile(
        currentRung = 1,
        phase = Phase.INTRODUCTORY,
        rungStartDate = LocalDate.now(),
        dailyReminderHour = null,
        dailyReminderMinute = null,
        onboardingComplete = true
    )

    @Before
    fun setup() {
        profileRepo = FakeUserProfileRepository()
        dailyLogRepo = FakeDailyLogRepository()
        streakRepo = FakeStreakRepository()
        profileRepo.setProfile(testProfile)
    }

    private fun createViewModel() {
        viewModel = WorkoutViewModel(profileRepo, dailyLogRepo, streakRepo)
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        createViewModel()
        assertTrue(viewModel.uiState.value is WorkoutUiState.Loading)
    }

    @Test
    fun `loads exercises from profile rung`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is WorkoutUiState.Active)
        state as WorkoutUiState.Active
        assertEquals(5, state.exercises.size)
        assertEquals(0, state.currentExerciseIndex)
        assertEquals(2, state.currentReps)
    }

    @Test
    fun `incrementReps increases currentReps by 1`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.incrementReps()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(3, state.currentReps)
    }

    @Test
    fun `decrementReps decreases currentReps by 1`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.incrementReps()
        viewModel.decrementReps()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(2, state.currentReps)
    }

    @Test
    fun `decrementReps does not go below 0`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        repeat(5) { viewModel.decrementReps() }
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(0, state.currentReps)
    }

    @Test
    fun `completeExercise advances to next exercise`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeExercise()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(1, state.currentExerciseIndex)
        assertEquals(3, state.currentReps)
    }

    @Test
    fun `completeExercise on last exercise does nothing`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        repeat(4) { viewModel.completeExercise() }
        val stateBefore = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(4, stateBefore.currentExerciseIndex)
        viewModel.completeExercise()
        val stateAfter = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(4, stateAfter.currentExerciseIndex)
    }

    @Test
    fun `completeWorkout saves daily log and increments streak`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(dailyLogRepo.hasCompletedToday())
        assertEquals(1, streakRepo.streakState.value.streakCount)
    }

    @Test
    fun `completeWorkout saves correct rung in log`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        val log = dailyLogRepo.getLogForDate(LocalDate.now())
        assertNotNull(log)
        assertEquals(1, log!!.rung)
        assertTrue(log.completed)
    }

    @Test
    fun `completeWorkout with higher rung saves correct rung`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 20))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        val log = dailyLogRepo.getLogForDate(LocalDate.now())
        assertEquals(20, log!!.rung)
    }

    @Test
    fun `full workout flow - complete all exercises then finish`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state0 = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(0, state0.currentExerciseIndex)
        assertEquals("Flexao para Frente", state0.exercises[0].name)

        viewModel.completeExercise()
        val state1 = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(1, state1.currentExerciseIndex)

        viewModel.completeExercise()
        val state2 = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(2, state2.currentExerciseIndex)

        viewModel.completeExercise()
        val state3 = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(3, state3.currentExerciseIndex)

        viewModel.completeExercise()
        val state4 = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(4, state4.currentExerciseIndex)
        assertTrue(state4.exercises[4].isRunJump)

        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(dailyLogRepo.hasCompletedToday())
    }

    @Test
    fun `last exercise is always run-jump`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertFalse(state.exercises[0].isRunJump)
        assertFalse(state.exercises[1].isRunJump)
        assertFalse(state.exercises[2].isRunJump)
        assertFalse(state.exercises[3].isRunJump)
        assertTrue(state.exercises[4].isRunJump)
    }

    @Test
    fun `loads lifetime exercises for rung 16`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 16, phase = Phase.LIFETIME))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(5, state.exercises.size)
        assertEquals(14, state.exercises[0].targetReps)
        assertEquals(10, state.exercises[1].targetReps)
        assertEquals(12, state.exercises[2].targetReps)
        assertEquals(9, state.exercises[3].targetReps)
    }

    @Test
    fun `loads lifetime run-jump with correct steps`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 16, phase = Phase.LIFETIME))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        val runJump = state.exercises[4]
        assertTrue(runJump.isRunJump)
        assertEquals(4, runJump.sets)
        assertEquals(40, runJump.extraSteps)
        assertEquals(10, runJump.jumpingJacksPerSet)
    }

    @Test
    fun `loads introductory run-jump with 7 jumping jacks`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(7, state.exercises[4].jumpingJacksPerSet)
    }

    @Test
    fun `completeExercise resets reps to next exercise target`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.incrementReps()
        viewModel.incrementReps()
        assertEquals(4, (viewModel.uiState.value as WorkoutUiState.Active).currentReps)
        viewModel.completeExercise()
        val state = viewModel.uiState.value as WorkoutUiState.Active
        assertEquals(1, state.currentExerciseIndex)
        assertEquals(3, state.currentReps)
    }

    @Test
    fun `multiple increment and decrement`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        repeat(10) { viewModel.incrementReps() }
        assertEquals(12, (viewModel.uiState.value as WorkoutUiState.Active).currentReps)
        repeat(5) { viewModel.decrementReps() }
        assertEquals(7, (viewModel.uiState.value as WorkoutUiState.Active).currentReps)
    }

    @Test
    fun `completeWorkout saves log with today date`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        val log = dailyLogRepo.getLogForDate(LocalDate.now())
        assertNotNull(log)
        assertEquals(LocalDate.now(), log!!.date)
    }

    @Test
    fun `completeWorkout on highest rung saves correct data`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 48, phase = Phase.LIFETIME))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        val log = dailyLogRepo.getLogForDate(LocalDate.now())
        assertEquals(48, log!!.rung)
        assertEquals(1, streakRepo.streakState.value.streakCount)
    }

    @Test
    fun `does not crash when profile is null during load`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is WorkoutUiState.Loading)
    }

    @Test
    fun `completeWorkout does not crash when profile is null`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.completeWorkout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(dailyLogRepo.hasCompletedToday())
    }
}
