package com.hackerfit.ui.screens.assessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.domain.model.AssessmentLog
import com.hackerfit.domain.model.WorkoutExercise
import com.hackerfit.domain.repository.AssessmentRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface AssessmentUiState {
    data object Loading : AssessmentUiState
    data class Ready(val nextRung: Int) : AssessmentUiState
    data class Workout(
        val exercises: List<WorkoutExercise>,
        val currentIndex: Int,
        val currentReps: Int
    ) : AssessmentUiState
    data class Evaluation(val nextRung: Int) : AssessmentUiState
}

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssessmentUiState>(AssessmentUiState.Loading)
    val uiState: StateFlow<AssessmentUiState> = _uiState.asStateFlow()

    private var exercises: List<WorkoutExercise> = emptyList()
    private var currentIndex = 0
    private var currentReps = 0
    private var nextRung = 0

    init {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile().first() ?: return@launch
            nextRung = (profile.currentRung + 1).coerceAtMost(48)
            _uiState.value = AssessmentUiState.Ready(nextRung)
        }
    }

    fun startAssessment() {
        val rungData = FitnessLadder.getRung(nextRung)
        val isIntro = FitnessLadder.isIntroductory(nextRung)
        val exDefs = FitnessLadder.exercises

        exercises = listOf(
            WorkoutExercise(
                index = 0,
                name = exDefs[0].name,
                description = if (isIntro) exDefs[0].introductoryDescription else exDefs[0].lifetimeDescription,
                targetReps = rungData.bend
            ),
            WorkoutExercise(
                index = 1,
                name = exDefs[1].name,
                description = if (isIntro) exDefs[1].introductoryDescription else exDefs[1].lifetimeDescription,
                targetReps = rungData.sitUp
            ),
            WorkoutExercise(
                index = 2,
                name = exDefs[2].name,
                description = if (isIntro) exDefs[2].introductoryDescription else exDefs[2].lifetimeDescription,
                targetReps = rungData.legLift
            ),
            WorkoutExercise(
                index = 3,
                name = exDefs[3].name,
                description = if (isIntro) exDefs[3].introductoryDescription else exDefs[3].lifetimeDescription,
                targetReps = rungData.pushUp
            ),
            WorkoutExercise(
                index = 4,
                name = exDefs[4].name,
                description = if (isIntro) exDefs[4].introductoryDescription else exDefs[4].lifetimeDescription,
                targetReps = rungData.runJumpSets * 75 + rungData.runJumpExtraSteps,
                isRunJump = true,
                sets = rungData.runJumpSets,
                extraSteps = rungData.runJumpExtraSteps,
                jumpingJacksPerSet = FitnessLadder.getJumpingJacksPerSet(nextRung)
            )
        )
        currentIndex = 0
        currentReps = exercises[0].targetReps
        _uiState.value = AssessmentUiState.Workout(exercises, 0, currentReps)
    }

    fun incrementReps() {
        currentReps++
        _uiState.value = AssessmentUiState.Workout(exercises, currentIndex, currentReps)
    }

    fun decrementReps() {
        if (currentReps > 0) currentReps--
        _uiState.value = AssessmentUiState.Workout(exercises, currentIndex, currentReps)
    }

    fun completeExercise() {
        if (currentIndex < exercises.lastIndex) {
            currentIndex++
            currentReps = exercises[currentIndex].targetReps
            _uiState.value = AssessmentUiState.Workout(exercises, currentIndex, currentReps)
        }
    }

    fun finishWorkout() {
        _uiState.value = AssessmentUiState.Evaluation(nextRung)
    }

    fun evaluate(passed: Boolean) {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile().first() ?: return@launch
            assessmentRepository.saveAssessment(
                AssessmentLog(
                    date = LocalDate.now(),
                    fromRung = profile.currentRung,
                    toRung = nextRung,
                    passed = passed
                )
            )
            if (passed) {
                userProfileRepository.updateRung(nextRung)
            }
        }
    }
}
