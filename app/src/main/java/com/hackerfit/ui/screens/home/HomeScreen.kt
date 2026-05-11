package com.hackerfit.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.domain.model.WorkoutExercise
import com.hackerfit.ui.components.ExerciseCard
import com.hackerfit.ui.components.StreakBadge
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onStartAssessment: () -> Unit,
    onViewLadder: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is HomeUiState.Success -> {
            HomeContent(
                state = state,
                innerPadding = innerPadding,
                onStartWorkout = onStartWorkout,
                onStartAssessment = onStartAssessment,
                onViewLadder = onViewLadder
            )
        }
        is HomeUiState.NotOnboarded -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    innerPadding: PaddingValues,
    onStartWorkout: () -> Unit,
    onStartAssessment: () -> Unit,
    onViewLadder: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val rung = remember(state.currentRung) { FitnessLadder.getRung(state.currentRung) }
    val phase = remember(state.currentRung) {
        if (FitnessLadder.isIntroductory(state.currentRung)) "Escada Introdut\u00f3ria" else "Escada Vital\u00edcia"
    }
    val daysOnRung = remember(state.rungStartDate) {
        ChronoUnit.DAYS.between(state.rungStartDate, today) + 1
    }
    val effectiveDaysOnRung = if (!state.completedToday && daysOnRung == 1L) 0L else daysOnRung
    val exercisesCompleted = if (state.completedToday) 5 else 0
    val canAssess = daysOnRung >= 5 && !state.completedToday
    val progress = (exercisesCompleted / 5f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "ringProgress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "title") {
            Text(
                text = "HackerFit",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item(key = "streak") {
            StreakBadge(
                streakCount = state.streakCount,
                freezesBanked = state.freezesBanked
            )
        }

        item(key = "current_rung") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewLadder() },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressRing(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$exercisesCompleted",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "/5",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Degrau ${state.currentRung}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = phase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${effectiveDaysOnRung} ${if (effectiveDaysOnRung == 1L) "dia" else "dias"} neste degrau",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ver escada",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (canAssess && state.currentRung < 48) {
            item(key = "assess_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tentar pr\u00f3ximo degrau?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Voc\u00ea est\u00e1 no degrau ${state.currentRung} h\u00e1 $effectiveDaysOnRung ${if (effectiveDaysOnRung == 1L) "dia" else "dias"}. Que tal tentar o degrau ${state.currentRung + 1}?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onStartAssessment,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Tentar Degrau ${state.currentRung + 1}")
                        }
                    }
                }
            }
        }

        if (state.completedToday) {
            item(key = "completed_today") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Treino de hoje conclu\u00eddo!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Volte amanha para continuar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            item(key = "workout_header") {
                Text(
                    text = "Treino de Hoje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(FitnessLadder.exercises, key = { idx, _ -> "exercise_$idx" }) { idx, exercise ->
                val target = when (idx) {
                    0 -> rung.bend
                    1 -> rung.sitUp
                    2 -> rung.legLift
                    3 -> rung.pushUp
                    4 -> rung.runJumpSets
                    else -> 0
                }
                val extra = if (idx == 4) rung.runJumpExtraSteps else 0
                val isIntro = FitnessLadder.isIntroductory(state.currentRung)
                val workoutEx = WorkoutExercise(
                    index = idx,
                    name = exercise.name,
                    description = if (isIntro) exercise.introductoryDescription else exercise.lifetimeDescription,
                    isRunJump = idx == 4,
                    targetReps = target,
                    sets = target,
                    jumpingJacksPerSet = FitnessLadder.getJumpingJacksPerSet(state.currentRung),
                    extraSteps = extra
                )
                ExerciseCard(exercise = workoutEx, targetReps = target)
            }

            item(key = "start_workout") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Iniciar Treino")
                }
            }
        }

        item(key = "spacer") { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(radius * 2, radius * 2)

        drawArc(
            color = surfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
