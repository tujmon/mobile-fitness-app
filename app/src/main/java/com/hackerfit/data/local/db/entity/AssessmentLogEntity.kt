package com.hackerfit.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "assessment_log",
    indices = [Index(value = ["date"])]
)
data class AssessmentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val fromRung: Int,
    val toRung: Int,
    val passed: Boolean,
    val notes: String? = null
)
