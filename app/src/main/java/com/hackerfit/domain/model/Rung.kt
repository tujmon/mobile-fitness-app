package com.hackerfit.domain.model

enum class Phase { INTRODUCTORY, LIFETIME }

data class WorkoutExercise(
    val index: Int,
    val name: String,
    val description: String,
    val targetReps: Int,
    val isRunJump: Boolean = false,
    val sets: Int = 0,
    val extraSteps: Int = 0,
    val jumpingJacksPerSet: Int = 0
)
