package com.hackerfit.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hackerfit.domain.model.StreakData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "streak")

@Singleton
class StreakDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val FREEZES_BANKED = intPreferencesKey("freezes_banked")
        val LAST_FREEZE_EARN_DATE = stringPreferencesKey("last_freeze_earn_date")
    }

    val streakData: Flow<StreakData> = context.streakDataStore.data.map { prefs ->
        StreakData(
            streakCount = prefs[Keys.STREAK_COUNT] ?: 0,
            freezesBanked = prefs[Keys.FREEZES_BANKED] ?: 0,
            lastFreezeEarnDate = prefs[Keys.LAST_FREEZE_EARN_DATE]?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
        )
    }

    suspend fun updateStreakData(data: StreakData) {
        context.streakDataStore.edit { prefs ->
            prefs[Keys.STREAK_COUNT] = data.streakCount
            prefs[Keys.FREEZES_BANKED] = data.freezesBanked
            prefs[Keys.LAST_FREEZE_EARN_DATE] = data.lastFreezeEarnDate?.toString() ?: ""
        }
    }

    suspend fun clear() {
        context.streakDataStore.edit { it.clear() }
    }
}
