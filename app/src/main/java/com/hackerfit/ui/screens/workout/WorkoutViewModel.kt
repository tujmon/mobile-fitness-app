package com.hackerfit.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.domain.model.DailyLog
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.WorkoutExercise
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Active(
        val exercises: List<WorkoutExercise>,
        val currentExerciseIndex: Int,
        val currentReps: Int
    ) : WorkoutUiState
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var exercises: List<WorkoutExercise> = emptyList()
    private var currentIndex = 0
    private var currentReps = 0

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile().first() ?: return@launch
            val rungNumber = profile.currentRung
            val rungData = FitnessLadder.getRung(rungNumber)
            val isIntro = FitnessLadder.isIntroductory(rungNumber)
            val exDefs = FitnessLadder.exercises

            exercises = listOf(
                WorkoutExercise(
                    name = exDefs[0].name,
                    description = if (isIntro) exDefs[0].introductoryDescription else exDefs[0].lifetimeDescription,
                    targetReps = rungData.bend
                ),
                WorkoutExercise(
                    name = exDefs[1].name,
                    description = if (isIntro) exDefs[1].introductoryDescription else exDefs[1].lifetimeDescription,
                    targetReps = rungData.sitUp
                ),
                WorkoutExercise(
                    name = exDefs[2].name,
                    description = if (isIntro) exDefs[2].introductoryDescription else exDefs[2].lifetimeDescription,
                    targetReps = rungData.legLift
                ),
                WorkoutExercise(
                    name = exDefs[3].name,
                    description = if (isIntro) exDefs[3].introductoryDescription else exDefs[3].lifetimeDescription,
                    targetReps = rungData.pushUp
                ),
                WorkoutExercise(
                    name = exDefs[4].name,
                    description = if (isIntro) exDefs[4].introductoryDescription else exDefs[4].lifetimeDescription,
                    targetReps = rungData.runJumpSets * 75 + rungData.runJumpExtraSteps,
                    isRunJump = true,
                    sets = rungData.runJumpSets,
                    extraSteps = rungData.runJumpExtraSteps,
                    jumpingJacksPerSet = FitnessLadder.getJumpingJacksPerSet(rungNumber)
                )
            )

            currentReps = exercises[0].targetReps
            _uiState.value = WorkoutUiState.Active(exercises, 0, currentReps)
        }
    }

    fun incrementReps() {
        currentReps++
        updateState()
    }

    fun decrementReps() {
        if (currentReps > 0) currentReps--
        updateState()
    }

    fun completeExercise() {
        if (currentIndex < exercises.lastIndex) {
            currentIndex++
            currentReps = exercises[currentIndex].targetReps
            updateState()
        }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile().first() ?: return@launch
            dailyLogRepository.saveLog(
                DailyLog(
                    date = LocalDate.now(),
                    rung = profile.currentRung,
                    completed = true,
                    completedAt = LocalDate.now()
                )
            )
            streakRepository.incrementStreak()
        }
    }

    private fun updateState() {
        _uiState.value = WorkoutUiState.Active(exercises, currentIndex, currentReps)
    }
}
