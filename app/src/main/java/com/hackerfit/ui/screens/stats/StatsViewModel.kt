package com.hackerfit.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.repository.AssessmentRepository
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DayActivity(
    val date: LocalDate,
    val dayLabel: String,
    val completed: Boolean,
    val rung: Int?
)

data class RungProgressEntry(
    val id: Long,
    val date: LocalDate,
    val fromRung: Int,
    val toRung: Int
)

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data class Success(
        val totalWorkouts: Int,
        val currentStreak: Int,
        val bestStreak: Int,
        val currentRung: Int,
        val weeklyData: List<DayActivity>,
        val rungProgression: List<RungProgressEntry>
    ) : StatsUiState
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dailyLogRepository: DailyLogRepository,
    private val assessmentRepository: AssessmentRepository,
    private val streakRepository: StreakRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val dayLabels = listOf("D", "S", "T", "Q", "Q", "S", "S")

    val uiState: StateFlow<StatsUiState> = combine(
        dailyLogRepository.getAllLogs(),
        assessmentRepository.getAllAssessments(),
        streakRepository.getStreakData(),
        userProfileRepository.getProfile()
    ) { logs, assessments, streak, profile ->
        val completedDates = logs.filter { it.completed }.map { it.date }.distinct()
        val bestStreak = maxOf(calculateMaxStreak(completedDates), streak.streakCount)

        val logByDate = logs.associateBy { it.date }
        val weeklyData = (6 downTo 0).map { daysAgo ->
            val date = LocalDate.now().minusDays(daysAgo.toLong())
            val log = logByDate[date]
            DayActivity(
                date = date,
                dayLabel = dayLabels[date.dayOfWeek.value % 7],
                completed = log?.completed == true,
                rung = log?.rung
            )
        }

        val rungProgression = assessments.filter { it.passed }.map {
            RungProgressEntry(it.id, it.date, it.fromRung, it.toRung)
        }

        StatsUiState.Success(
            totalWorkouts = completedDates.size,
            currentStreak = streak.streakCount,
            bestStreak = bestStreak,
            currentRung = profile?.currentRung ?: 1,
            weeklyData = weeklyData,
            rungProgression = rungProgression
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState.Loading)

    private fun calculateMaxStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sorted()
        var maxStreak = 1
        var current = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].plusDays(1)) {
                current++
                maxStreak = maxOf(maxStreak, current)
            } else {
                current = 1
            }
        }
        return maxStreak
    }
}
