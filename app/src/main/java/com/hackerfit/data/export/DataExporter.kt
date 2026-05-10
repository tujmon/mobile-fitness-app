package com.hackerfit.data.export

import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.StreakData
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

object DataExporter {

    fun exportToJson(
        profile: UserProfileEntity?,
        logs: List<DailyLogEntity>,
        assessments: List<AssessmentLogEntity>,
        streak: StreakData
    ): String {
        val root = JSONObject().apply {
            put("version", 1)
            put("app", "HackerFit")
            put("exportDate", LocalDateTime.now().toString())

            put("profile", profile?.let { p ->
                JSONObject().apply {
                    put("currentRung", p.currentRung)
                    put("phase", p.phase)
                    put("rungStartDate", p.rungStartDate.toString())
                    put("dailyReminderHour", p.dailyReminderHour ?: JSONObject.NULL)
                    put("dailyReminderMinute", p.dailyReminderMinute ?: JSONObject.NULL)
                    put("onboardingComplete", p.onboardingComplete)
                }
            })

            put("streak", JSONObject().apply {
                put("streakCount", streak.streakCount)
                put("freezesBanked", streak.freezesBanked)
                put("lastFreezeEarnDate", streak.lastFreezeEarnDate?.toString() ?: "")
            })

            put("dailyLogs", JSONArray().apply {
                logs.forEach { log ->
                    put(JSONObject().apply {
                        put("date", log.date.toString())
                        put("rung", log.rung)
                        put("completed", log.completed)
                        put("completedAt", log.completedAt?.toString() ?: JSONObject.NULL)
                    })
                }
            })

            put("assessmentLogs", JSONArray().apply {
                assessments.forEach { a ->
                    put(JSONObject().apply {
                        put("date", a.date.toString())
                        put("fromRung", a.fromRung)
                        put("toRung", a.toRung)
                        put("passed", a.passed)
                        put("notes", a.notes ?: "")
                    })
                }
            })
        }
        return root.toString(2)
    }
}
