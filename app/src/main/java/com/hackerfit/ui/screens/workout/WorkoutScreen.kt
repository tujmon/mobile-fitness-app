package com.hackerfit.ui.screens.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.model.WorkoutExercise
import com.hackerfit.ui.components.RepCounter

@Composable
fun WorkoutScreen(
    onFinish: () -> Unit,
    onExerciseInfo: (Int) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is WorkoutUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WorkoutUiState.Active -> {
            WorkoutContent(
                exercises = state.exercises,
                currentIndex = state.currentExerciseIndex,
                currentReps = state.currentReps,
                onIncrement = viewModel::incrementReps,
                onDecrement = viewModel::decrementReps,
                onCompleteExercise = viewModel::completeExercise,
                onFinish = {
                    viewModel.completeWorkout()
                    onFinish()
                },
                onExerciseInfo = onExerciseInfo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutContent(
    exercises: List<WorkoutExercise>,
    currentIndex: Int,
    currentReps: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onCompleteExercise: () -> Unit,
    onFinish: () -> Unit,
    onExerciseInfo: (Int) -> Unit
) {
    val exercise = exercises[currentIndex]
    val isLast = currentIndex == exercises.lastIndex
    val progress = (currentIndex + 1).toFloat() / exercises.size
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Exercicio ${currentIndex + 1} de ${exercises.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onExerciseInfo(currentIndex) }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info do exercicio",
                    modifier = Modifier.scale(1.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (exercise.isRunJump) {
            RunJumpTarget(exercise = exercise)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = if (isLast) onFinish else onCompleteExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isLast) "Finalizar Treino" else "Concluir Exerc\u00edcio",
                    fontSize = 18.sp
                )
            }
        } else {
            RepCounter(
                currentReps = currentReps,
                targetReps = exercise.targetReps,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = if (isLast) onFinish else onCompleteExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isLast) "Finalizar Treino" else "Concluir Exercicio",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RunJumpTarget(exercise: WorkoutExercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Corrida e Salto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${exercise.sets} sets de 75 passos + ${exercise.jumpingJacksPerSet} polichinelos",
                style = MaterialTheme.typography.bodyLarge
            )
            if (exercise.extraSteps > 0) {
                Text(
                    text = "+ ${exercise.extraSteps} passos extras",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
