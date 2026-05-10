package com.hackerfit.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.data.export.DataExporter
import com.hackerfit.data.export.DataImporter
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.preferences.StreakDataStore
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Confirm(val logCount: Int, val assessmentCount: Int) : ImportState()
    data object Done : ImportState()
    data class Error(val message: String) : ImportState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userProfileDao: UserProfileDao,
    private val dailyLogDao: DailyLogDao,
    private val assessmentLogDao: AssessmentLogDao,
    private val streakDataStore: StreakDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private var pendingImport: DataImporter.ImportedData? = null

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

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val profile = userProfileDao.getProfileOnce()
                val logs = dailyLogDao.getAllLogsList()
                val assessments = assessmentLogDao.getAllAssessmentsList()
                val streak = streakDataStore.streakData.first()
                val json = DataExporter.exportToJson(profile, logs, assessments, streak)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
            } catch (_: Exception) {}
        }
    }

    fun readImportData(uri: Uri) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val json = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: throw Exception("Nao foi possivel ler o arquivo")
                val data = DataImporter.parseFromJson(json)
                pendingImport = data
                _importState.value = ImportState.Confirm(
                    logCount = data.logs.size,
                    assessmentCount = data.assessments.size
                )
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Erro ao importar")
            }
        }
    }

    fun confirmImport(replace: Boolean) {
        viewModelScope.launch {
            val data = pendingImport ?: return@launch
            try {
                if (replace) {
                    dailyLogDao.deleteAll()
                    assessmentLogDao.deleteAll()
                    streakDataStore.clear()
                }
                data.profile?.let { userProfileDao.saveProfile(it) }
                if (replace) {
                    dailyLogDao.insertAll(data.logs)
                    assessmentLogDao.insertAll(data.assessments)
                } else {
                    val existingLogs = dailyLogDao.getAllLogsList().associateBy { it.date }
                    val mergedLogs = data.logs.map { imported ->
                        existingLogs[imported.date]?.let { existing ->
                            if (existing.completed) existing else imported
                        } ?: imported
                    }
                    dailyLogDao.insertAll(mergedLogs)
                    assessmentLogDao.insertAll(data.assessments)
                }
                streakDataStore.updateStreakData(data.streak)
                pendingImport = null
                _importState.value = ImportState.Done
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Erro ao importar")
            }
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }
}
