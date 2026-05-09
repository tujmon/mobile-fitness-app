package com.hackerfit.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.ui.components.StreakBadge
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onStartAssessment: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                onStartAssessment = onStartAssessment
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
    onStartAssessment: () -> Unit
) {
    val rung = FitnessLadder.getRung(state.currentRung)
    val phase = if (FitnessLadder.isIntroductory(state.currentRung)) "Escada Introdut\u00f3ria" else "Escada Vital\u00edcia"
    val daysOnRung = ChronoUnit.DAYS.between(state.rungStartDate, LocalDate.now()) + 1
    val canAssess = daysOnRung >= 5 && !state.completedToday

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "HackerFit",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        StreakBadge(
            streakCount = state.streakCount,
            freezesBanked = state.freezesBanked
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                    text = "$daysOnRung dia${if (daysOnRung != 1L) "s" else ""} neste degrau",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.completedToday) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\u2705 Treino de hoje conclu\u00eddo!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Volte amanh\u00e3 para continuar.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(
                text = "Treino de Hoje",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val exercises = FitnessLadder.exercises
            val exerciseItems = listOf(
                "${exercises[0].name}: ${rung.bend}x",
                "${exercises[1].name}: ${rung.sitUp}x",
                "${exercises[2].name}: ${rung.legLift}x",
                "${exercises[3].name}: ${rung.pushUp}x",
                "${exercises[4].name}: ${rung.runJumpSets} sets + ${rung.runJumpExtraSteps} passos"
            )

            exerciseItems.forEach { item ->
                Text(
                    text = "\u2022 $item",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "Iniciar Treino", fontSize = 18.sp)
            }
        }

        if (canAssess && state.currentRung < 48) {
            Spacer(modifier = Modifier.height(16.dp))

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
                        text = "Voc\u00ea est\u00e1 no degrau ${state.currentRung} h\u00e1 $daysOnRung dias. Que tal tentar o degrau ${state.currentRung + 1}?",
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
