package com.hackerfit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.preferences.StreakDataStore
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val reminderEnabled: Boolean,
        val hour: Int,
        val minute: Int
    ) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val dailyLogDao: DailyLogDao,
    private val assessmentLogDao: AssessmentLogDao,
    private val streakDataStore: StreakDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userProfileRepository.getProfile().collect { profile ->
                if (profile != null) {
                    _uiState.value = SettingsUiState.Success(
                        reminderEnabled = profile.dailyReminderHour != null,
                        hour = profile.dailyReminderHour ?: 8,
                        minute = profile.dailyReminderMinute ?: 0
                    )
                }
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userProfileRepository.setReminderTime(hour, minute)
        }
    }

    fun disableReminder() {
        viewModelScope.launch {
            userProfileRepository.clearReminderTime()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            dailyLogDao.deleteAll()
            assessmentLogDao.deleteAll()
            streakDataStore.clear()
        }
    }
}
