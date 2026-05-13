package com.hackerfit.domain.policy

import javax.inject.Inject

sealed class ReminderImportDecision {
    data class Schedule(val hour: Int, val minute: Int) : ReminderImportDecision()
    data object CancelOnly : ReminderImportDecision()
    data class ClearAndCancel(val warningMessage: String) : ReminderImportDecision()
}

class ReminderImportPolicy @Inject constructor() {
    fun resolve(
        reminderHour: Int?,
        reminderMinute: Int?,
        hasNotificationPermission: Boolean
    ): ReminderImportDecision {
        if (reminderHour != null) {
            return if (hasNotificationPermission) {
                ReminderImportDecision.Schedule(reminderHour, reminderMinute ?: 0)
            } else {
                ReminderImportDecision.ClearAndCancel(
                    "Dados importados, mas o lembrete foi desativado porque a permiss\u00e3o de notifica\u00e7\u00e3o n\u00e3o foi concedida."
                )
            }
        }
        return ReminderImportDecision.CancelOnly
    }
}
