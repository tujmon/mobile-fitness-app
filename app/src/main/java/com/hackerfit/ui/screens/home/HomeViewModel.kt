package com.hackerfit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.model.DailyLog
import com.hackerfit.domain.model.StreakData
import com.hackerfit.domain.model.UserProfile
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object NotOnboarded : HomeUiState
    data class Success(
        val currentRung: Int,
        val rungStartDate: LocalDate,
        val completedToday: Boolean,
        val streakCount: Int,
        val freezesBanked: Int
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userProfileRepository.getProfile(),
        streakRepository.getStreakData(),
        dailyLogRepository.observeCompletedToday()
    ) { profile, streak, completedToday ->
        if (profile == null) {
            HomeUiState.NotOnboarded
        } else {
            HomeUiState.Success(
                currentRung = profile.currentRung,
                rungStartDate = profile.rungStartDate,
                completedToday = completedToday,
                streakCount = streak.streakCount,
                freezesBanked = streak.freezesBanked
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)
}
