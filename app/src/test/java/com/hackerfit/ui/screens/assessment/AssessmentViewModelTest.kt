package com.hackerfit.ui.screens.assessment

import app.cash.turbine.test
import com.hackerfit.FakeAssessmentRepository
import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.UserProfile
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AssessmentViewModelTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private lateinit var assessmentRepo: FakeAssessmentRepository
    private lateinit var viewModel: AssessmentViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testProfile = UserProfile(
        currentRung = 5,
        phase = Phase.INTRODUCTORY,
        rungStartDate = LocalDate.now().minusDays(10),
        dailyReminderHour = null,
        dailyReminderMinute = null,
        onboardingComplete = true
    )

    @Before
    fun setup() {
        profileRepo = FakeUserProfileRepository()
        assessmentRepo = FakeAssessmentRepository()
        profileRepo.setProfile(testProfile)
    }

    private fun createViewModel() {
        viewModel = AssessmentViewModel(profileRepo, assessmentRepo)
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        createViewModel()
        assertTrue(viewModel.uiState.value is AssessmentUiState.Loading)
    }

    @Test
    fun `loads Ready state with next rung`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is AssessmentUiState.Ready)
        assertEquals(6, (state as AssessmentUiState.Ready).nextRung)
    }

    @Test
    fun `next rung is capped at 48`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 48))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value as AssessmentUiState.Ready
        assertEquals(48, state.nextRung)
    }

    @Test
    fun `startAssessment creates Workout state with 5 exercises`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        val state = viewModel.uiState.value
        assertTrue(state is AssessmentUiState.Workout)
        state as AssessmentUiState.Workout
        assertEquals(5, state.exercises.size)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `incrementReps increases currentReps`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.incrementReps()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(state.exercises[0].targetReps + 1, state.currentReps)
    }

    @Test
    fun `decrementReps decreases currentReps`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.incrementReps()
        viewModel.decrementReps()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(state.exercises[0].targetReps, state.currentReps)
    }

    @Test
    fun `decrementReps does not go below 0`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        repeat(20) { viewModel.decrementReps() }
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(0, state.currentReps)
    }

    @Test
    fun `completeExercise advances to next exercise`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.completeExercise()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(1, state.currentIndex)
    }

    @Test
    fun `completeExercise on last exercise does nothing`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        repeat(4) { viewModel.completeExercise() }
        viewModel.completeExercise()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(4, state.currentIndex)
    }

    @Test
    fun `finishWorkout transitions to Evaluation state`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        val state = viewModel.uiState.value
        assertTrue(state is AssessmentUiState.Evaluation)
        assertEquals(6, (state as AssessmentUiState.Evaluation).nextRung)
    }

    @Test
    fun `evaluate with pass saves assessment and updates rung`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        viewModel.evaluate(passed = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val assessments = assessmentRepo.assessmentsState.value
        assertEquals(1, assessments.size)
        assertEquals(5, assessments[0].fromRung)
        assertEquals(6, assessments[0].toRung)
        assertTrue(assessments[0].passed)

        assertEquals(6, profileRepo.profileState.value?.currentRung)
    }

    @Test
    fun `evaluate with fail saves assessment but does not update rung`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        viewModel.evaluate(passed = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val assessments = assessmentRepo.assessmentsState.value
        assertEquals(1, assessments.size)
        assertFalse(assessments[0].passed)

        assertEquals(5, profileRepo.profileState.value?.currentRung)
    }

    @Test
    fun `full assessment flow - complete all exercises and pass`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val ready = viewModel.uiState.value as AssessmentUiState.Ready
        assertEquals(6, ready.nextRung)

        viewModel.startAssessment()
        repeat(3) { viewModel.completeExercise() }
        val workout = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(3, workout.currentIndex)

        viewModel.finishWorkout()
        assertTrue(viewModel.uiState.value is AssessmentUiState.Evaluation)

        viewModel.evaluate(passed = true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(6, profileRepo.profileState.value?.currentRung)
    }

    @Test
    fun `assessment loads lifetime exercises for next rung 16`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 15, phase = Phase.INTRODUCTORY))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val ready = viewModel.uiState.value as AssessmentUiState.Ready
        assertEquals(16, ready.nextRung)
        viewModel.startAssessment()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertEquals(14, state.exercises[0].targetReps)
    }

    @Test
    fun `assessment for rung 48 caps at 48`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 47))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val ready = viewModel.uiState.value as AssessmentUiState.Ready
        assertEquals(48, ready.nextRung)
    }

    @Test
    fun `evaluate pass from rung 1 to rung 2`() = runTest(testDispatcher) {
        profileRepo.setProfile(testProfile.copy(currentRung = 1))
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        viewModel.evaluate(passed = true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, profileRepo.profileState.value?.currentRung)
        val assessments = assessmentRepo.assessmentsState.value
        assertEquals(1, assessments[0].fromRung)
        assertEquals(2, assessments[0].toRung)
    }

    @Test
    fun `evaluate saves assessment log with today date`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        viewModel.evaluate(passed = true)
        testDispatcher.scheduler.advanceUntilIdle()
        val assessment = assessmentRepo.assessmentsState.value[0]
        assertEquals(LocalDate.now(), assessment.date)
    }

    @Test
    fun `startAssessment resets to first exercise`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.completeExercise()
        assertEquals(1, (viewModel.uiState.value as AssessmentUiState.Workout).currentIndex)
        viewModel.startAssessment()
        assertEquals(0, (viewModel.uiState.value as AssessmentUiState.Workout).currentIndex)
    }

    @Test
    fun `finishWorkout can be called at any exercise`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        viewModel.finishWorkout()
        assertTrue(viewModel.uiState.value is AssessmentUiState.Evaluation)
    }

    @Test
    fun `last exercise in assessment is run-jump`() = runTest(testDispatcher) {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startAssessment()
        val state = viewModel.uiState.value as AssessmentUiState.Workout
        assertTrue(state.exercises.last().isRunJump)
        assertFalse(state.exercises.first().isRunJump)
    }
}
