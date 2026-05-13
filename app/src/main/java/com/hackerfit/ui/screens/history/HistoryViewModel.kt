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
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class RecentLog(
        val id: Long,
        val date: LocalDate,
        val rung: Int,
        val completed: Boolean
    )

    data class AssessmentEntry(
        val id: Long,
        val date: LocalDate,
        val fromRung: Int,
        val toRung: Int,
        val passed: Boolean,
        val notes: String?
    )

    data class DayDetail(
        val id: Long,
        val date: LocalDate,
        val rung: Int,
        val completed: Boolean
    )

    data class Success(
        val selectedMonth: YearMonth,
        val completedDates: Set<LocalDate>,
        val logByDate: Map<LocalDate, RecentLog>,
        val selectedDay: LocalDate?,
        val dayDetail: DayDetail?,
        val recentLogs: List<RecentLog>,
        val assessments: List<AssessmentEntry>
    ) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dailyLogRepository: DailyLogRepository,
    private val assessmentRepository: AssessmentRepository,
    private val streakRepository: StreakRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDay = MutableStateFlow<LocalDate?>(null)

    init {
        viewModelScope.launch {
            combine(
                dailyLogRepository.getAllLogs(),
                assessmentRepository.getAllAssessments(),
                _selectedMonth,
                _selectedDay
            ) { logs, assessments, month, day ->
                val monthLogs = logs.filter {
                    YearMonth.from(it.date) == month
                }
                val logMap = logs.associate {
                    it.date to HistoryUiState.RecentLog(it.id, it.date, it.rung, it.completed)
                }
                val detail = day?.let { d ->
                    logMap[d]?.let { log ->
                        HistoryUiState.DayDetail(log.id, d, log.rung, log.completed)
                    }
                }

                HistoryUiState.Success(
                    selectedMonth = month,
                    completedDates = logs.filter { it.completed }.map { it.date }.toSet(),
                    logByDate = logMap,
                    selectedDay = day,
                    dayDetail = detail,
                    recentLogs = monthLogs.map {
                        HistoryUiState.RecentLog(it.id, it.date, it.rung, it.completed)
                    },
                    assessments = assessments.map {
                        HistoryUiState.AssessmentEntry(
                            id = it.id,
                            date = it.date,
                            fromRung = it.fromRung,
                            toRung = it.toRung,
                            passed = it.passed,
                            notes = it.notes
                        )
                    }
                )
            }.collect { _uiState.value = it }
        }
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun selectDay(date: LocalDate?) {
        _selectedDay.value = date
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            dailyLogRepository.deleteLog(id)
            streakRepository.recalculateStreak()
        }
    }

    fun deleteAssessment(id: Long) {
        viewModelScope.launch {
            assessmentRepository.deleteAssessment(id)
            userProfileRepository.recalculateCurrentRung()
        }
    }

    fun updateLog(id: Long, date: LocalDate, rung: Int, completed: Boolean) {
        require(rung in 1..48) { "Degrau invalido: $rung" }
        viewModelScope.launch {
            dailyLogRepository.saveLog(
                DailyLog(
                    id = id,
                    date = date,
                    rung = rung,
                    completed = completed,
                    completedAt = if (completed) date else null
                )
            )
            streakRepository.recalculateStreak()
        }
    }

    fun updateAssessment(
        id: Long,
        date: LocalDate,
        fromRung: Int,
        toRung: Int,
        passed: Boolean,
        notes: String?
    ) {
        require(fromRung in 1..48) { "fromRung invalido: $fromRung" }
        require(toRung in 1..48) { "toRung invalido: $toRung" }
        require(!passed || toRung == fromRung + 1) { "Assessment reprovado nao pode ter toRung != fromRung + 1" }
        viewModelScope.launch {
            assessmentRepository.saveAssessment(
                AssessmentLog(
                    id = id,
                    date = date,
                    fromRung = fromRung,
                    toRung = toRung,
                    passed = passed,
                    notes = notes
                )
            )
            userProfileRepository.recalculateCurrentRung()
        }
    }
}
