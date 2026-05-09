package com.hackerfit.domain.model

enum class Phase { INTRODUCTORY, LIFETIME }

data class Rung(
    val number: Int,
    val phase: Phase,
    val bend: Int,
    val sitUp: Int,
    val legLift: Int,
    val pushUp: Int,
    val runJumpSets: Int,
    val runJumpExtraSteps: Int,
    val jumpingJacksPerSet: Int
)

data class WorkoutExercise(
    val name: String,
    val description: String,
    val targetReps: Int,
    val isRunJump: Boolean = false,
    val sets: Int = 0,
    val extraSteps: Int = 0,
    val jumpingJacksPerSet: Int = 0
)

data class WorkoutSession(
    val rung: Rung,
    val exercises: List<WorkoutExercise>
)
