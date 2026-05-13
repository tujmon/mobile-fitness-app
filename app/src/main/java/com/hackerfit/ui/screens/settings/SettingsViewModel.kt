package com.hackerfit.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.data.export.DataExporter
import com.hackerfit.data.export.DataImporter
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.data.local.preferences.StreakDataStore
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import com.hackerfit.service.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
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

sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data object Done : ExportState()
    data class Error(val message: String) : ExportState()
}

private data class AssessmentDedupKey(
    val date: LocalDate,
    val fromRung: Int,
    val toRung: Int,
    val passed: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userProfileDao: UserProfileDao,
    private val dailyLogDao: DailyLogDao,
    private val assessmentLogDao: AssessmentLogDao,
    private val streakDataStore: StreakDataStore,
    private val streakRepository: StreakRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

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
            ReminderScheduler.schedule(context, hour, minute)
        }
    }

    fun disableReminder() {
        viewModelScope.launch {
            userProfileRepository.clearReminderTime()
            ReminderScheduler.cancel(context)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            dailyLogDao.deleteAll()
            assessmentLogDao.deleteAll()
            streakDataStore.clear()
            ReminderScheduler.cancel(context)
            userProfileDao.saveProfile(
                UserProfileEntity(
                    currentRung = 1,
                    phase = "introductory",
                    rungStartDate = java.time.LocalDate.now(),
                    dailyReminderHour = null,
                    dailyReminderMinute = null,
                    onboardingComplete = false
                )
            )
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val profile = userProfileDao.getProfileOnce()
                val logs = dailyLogDao.getAllLogsList()
                val assessments = assessmentLogDao.getAllAssessmentsList()
                val streak = streakDataStore.streakData.first()
                val json = DataExporter.exportToJson(profile, logs, assessments, streak)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw Exception("Nao foi possivel abrir arquivo para escrita")
                _exportState.value = ExportState.Done
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Erro ao exportar")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
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
                    val profileToSave = data.profile ?: UserProfileEntity(
                        currentRung = 1,
                        phase = "introductory",
                        rungStartDate = java.time.LocalDate.now(),
                        dailyReminderHour = null,
                        dailyReminderMinute = null,
                        onboardingComplete = false
                    )
                    userProfileDao.saveProfile(profileToSave)
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
                    val existingAssessments = assessmentLogDao.getAllAssessmentsList()
                    val existingKeys = existingAssessments.map {
                        AssessmentDedupKey(it.date, it.fromRung, it.toRung, it.passed)
                    }.toSet()
                    val newAssessments = data.assessments.filter { a ->
                        AssessmentDedupKey(a.date, a.fromRung, a.toRung, a.passed) !in existingKeys
                    }
                    assessmentLogDao.insertAll(newAssessments)
                }
                if (replace) {
                    data.streak.let { streakDataStore.updateStreakData(it) }
                    streakRepository.recalculateStreak()
                    val savedProfile = userProfileDao.getProfileOnce()
                    if (savedProfile?.dailyReminderHour != null && hasNotificationPermission()) {
                        ReminderScheduler.schedule(context, savedProfile.dailyReminderHour, savedProfile.dailyReminderMinute ?: 0)
                    } else if (savedProfile?.dailyReminderHour == null) {
                        ReminderScheduler.cancel(context)
                    }
                } else {
                    userProfileRepository.recalculateCurrentRung()
                    streakRepository.recalculateStreak()
                }
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

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
