package com.hackerfit.ui.screens.assessment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.domain.model.WorkoutExercise

@Composable
fun AssessmentScreen(
    onFinish: () -> Unit,
    viewModel: AssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AssessmentUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AssessmentUiState.Ready -> {
            AssessmentReadyContent(
                nextRung = state.nextRung,
                onStart = viewModel::startAssessment
            )
        }
        is AssessmentUiState.Workout -> {
            AssessmentWorkoutContent(
                exercises = state.exercises,
                currentIndex = state.currentIndex,
                currentReps = state.currentReps,
                onIncrement = viewModel::incrementReps,
                onDecrement = viewModel::decrementReps,
                onCompleteExercise = viewModel::completeExercise,
                onDoneWorkingOut = viewModel::finishWorkout
            )
        }
        is AssessmentUiState.Evaluation -> {
            AssessmentEvaluationContent(
                nextRung = state.nextRung,
                onEasy = {
                    viewModel.evaluate(true)
                    onFinish()
                },
                onHard = {
                    viewModel.evaluate(false)
                    onFinish()
                }
            )
        }
    }
}

@Composable
private fun AssessmentReadyContent(nextRung: Int, onStart: () -> Unit) {
    val rungData = FitnessLadder.getRung(nextRung)
    val phase = if (FitnessLadder.isIntroductory(nextRung)) "Introdut\u00f3ria" else "Vital\u00edcia"

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Avalia\u00e7\u00e3o",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hoje voc\u00ea vai tentar o Degrau $nextRung ($phase)",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        if (nextRung == 16) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Text(
                    text = "Aten\u00e7\u00e3o: as repeti\u00e7\u00f5es diminuem porque os exerc\u00edcios ficam mais dif\u00edceis na Escada Vital\u00edcia!",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val exercises = FitnessLadder.exercises
        listOf(
            "${exercises[0].name}: ${rungData.bend}x",
            "${exercises[1].name}: ${rungData.sitUp}x",
            "${exercises[2].name}: ${rungData.legLift}x",
            "${exercises[3].name}: ${rungData.pushUp}x",
            "${exercises[4].name}: ${rungData.runJumpSets} sets + ${rungData.runJumpExtraSteps} passos"
        ).forEach {
            Text(text = "\u2022 $it", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Tentar Degrau $nextRung", fontSize = 18.sp)
        }
    }
}

@Composable
private fun AssessmentWorkoutContent(
    exercises: List<WorkoutExercise>,
    currentIndex: Int,
    currentReps: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onCompleteExercise: () -> Unit,
    onDoneWorkingOut: () -> Unit
) {
    val exercise = exercises[currentIndex]
    val isLast = currentIndex == exercises.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Avalia\u00e7\u00e3o \u2014 Exerc\u00edcio ${currentIndex + 1}/${exercises.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(text = exercise.description, modifier = Modifier.padding(12.dp), lineHeight = 22.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!exercise.isRunJump) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilledTonalButton(onClick = onDecrement, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(32.dp)) {
                    Text("-", fontSize = 28.sp)
                }
                Text("$currentReps", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                FilledTonalButton(onClick = onIncrement, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(32.dp)) {
                    Text("+", fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = if (isLast) onDoneWorkingOut else onCompleteExercise,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isLast) "Concluir Avalia\u00e7\u00e3o" else "Pr\u00f3ximo Exerc\u00edcio", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AssessmentEvaluationContent(
    nextRung: Int,
    onEasy: () -> Unit,
    onHard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Como voc\u00ea se sentiu?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Degrau $nextRung",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onEasy,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("\u2705 F\u00e1cil, posso avan\u00e7ar!", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onHard,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("\ud83d\udcaa Dif\u00edcil, vou ficar no atual", fontSize = 18.sp)
        }
    }
}
