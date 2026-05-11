package com.hackerfit.data.export

import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.StreakData
import org.json.JSONObject
import java.time.LocalDate

private const val MIN_RUNG = 1
private const val MAX_RUNG = 48
private val VALID_PHASES = setOf("introductory", "lifetime")

object DataImporter {

    data class ImportedData(
        val profile: UserProfileEntity?,
        val logs: List<DailyLogEntity>,
        val assessments: List<AssessmentLogEntity>,
        val streak: StreakData
    )

    fun parseFromJson(json: String): ImportedData {
        val root = JSONObject(json)

        val version = root.optInt("version", -1)
        if (version != 1) throw IllegalArgumentException("Versao nao suportada: $version")

        val profile = root.optJSONObject("profile")?.let { p ->
            val rung = p.getInt("currentRung")
            require(rung in MIN_RUNG..MAX_RUNG) { "Degrau invalido: $rung" }
            val rawPhase = p.getString("phase")
            require(rawPhase in VALID_PHASES) { "Fase invalida: $rawPhase" }
            UserProfileEntity(
                currentRung = rung,
                phase = rawPhase,
                rungStartDate = LocalDate.parse(p.getString("rungStartDate")),
                dailyReminderHour = if (p.isNull("dailyReminderHour")) null else p.getInt("dailyReminderHour"),
                dailyReminderMinute = if (p.isNull("dailyReminderMinute")) null else p.getInt("dailyReminderMinute"),
                onboardingComplete = p.getBoolean("onboardingComplete")
            )
        }

        val streak = root.getJSONObject("streak").let { s ->
            StreakData(
                streakCount = s.getInt("streakCount"),
                freezesBanked = s.getInt("freezesBanked"),
                lastFreezeEarnDate = s.optString("lastFreezeEarnDate")
                    .takeIf { it.isNotEmpty() }
                    ?.let { LocalDate.parse(it) }
            )
        }

        val logs = root.getJSONArray("dailyLogs").let { arr ->
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                DailyLogEntity(
                    date = LocalDate.parse(obj.getString("date")),
                    rung = obj.getInt("rung"),
                    completed = obj.getBoolean("completed"),
                    completedAt = if (obj.isNull("completedAt")) null
                        else LocalDate.parse(obj.getString("completedAt"))
                )
            }
        }

        val assessments = root.getJSONArray("assessmentLogs").let { arr ->
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                AssessmentLogEntity(
                    date = LocalDate.parse(obj.getString("date")),
                    fromRung = obj.getInt("fromRung"),
                    toRung = obj.getInt("toRung"),
                    passed = obj.getBoolean("passed"),
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() }
                )
            }
        }

        return ImportedData(profile, logs, assessments, streak)
    }
}
