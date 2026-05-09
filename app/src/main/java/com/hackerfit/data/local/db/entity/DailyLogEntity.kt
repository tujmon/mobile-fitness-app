package com.hackerfit.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_log")
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val rung: Int,
    val completed: Boolean,
    val completedAt: LocalDate? = null
)
