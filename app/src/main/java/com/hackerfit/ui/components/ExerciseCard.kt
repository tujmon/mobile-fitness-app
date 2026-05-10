package com.hackerfit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackerfit.domain.model.WorkoutExercise

@Composable
fun ExerciseCard(
    exercise: WorkoutExercise,
    targetReps: Int,
    modifier: Modifier = Modifier
) {
    val (icon, iconFilled) = getExerciseIcons(exercise.index)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = iconFilled,
                contentDescription = exercise.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (exercise.isRunJump) {
                        "${exercise.sets} sets + ${exercise.extraSteps} passos"
                    } else {
                        "$targetReps repeti\u00e7\u00f5es"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getExerciseIcons(index: Int): Pair<ImageVector, ImageVector> {
    return when (index) {
        0 -> Icons.Filled.AccessibilityNew to Icons.Filled.AccessibilityNew
        1 -> Icons.Filled.FitnessCenter to Icons.Filled.FitnessCenter
        2 -> Icons.Filled.FitnessCenter to Icons.Filled.FitnessCenter
        3 -> Icons.Filled.FitnessCenter to Icons.Filled.FitnessCenter
        4 -> Icons.Filled.DirectionsRun to Icons.Filled.DirectionsRun
        else -> Icons.Filled.FitnessCenter to Icons.Filled.FitnessCenter
    }
}
