package com.hackerfit.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.model.AssessmentLog
import com.hackerfit.domain.model.DailyLog
import com.hackerfit.domain.repository.AssessmentRepository
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(
        val totalWorkouts: Int,
        val maxStreak: Int,
        val recentLogs: List<DailyLog>,
        val assessments: List<AssessmentLog>
    ) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dailyLogRepository: DailyLogRepository,
    private val assessmentRepository: AssessmentRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dailyLogRepository.getAllLogs(),
                assessmentRepository.getAllAssessments(),
                streakRepository.getStreakData()
            ) { logs, assessments, streak ->
                HistoryUiState.Success(
                    totalWorkouts = logs.count { it.completed },
                    maxStreak = streak.streakCount,
                    recentLogs = logs.take(30),
                    assessments = assessments
                )
            }.collect { _uiState.value = it }
        }
    }
}
