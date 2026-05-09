package com.hackerfit.ui.screens.history

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
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    innerPadding: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hist\u00f3rico", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HistoryUiState.Success -> {
                    StatsCards(state)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Treinos Recentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.recentLogs.isEmpty()) {
                        Text("Nenhum treino registrado ainda.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.recentLogs.forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                    Text("Degrau ${log.rung}")
                                    if (log.completed) Text("\u2705") else Text("\u274c")
                                }
                            }
                        }
                    }

                    if (state.assessments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Avalia\u00e7\u00f5es", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        state.assessments.forEach { a ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(a.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                    Text("Degrau ${a.fromRung} \u2192 ${a.toRung}")
                                    if (a.passed) Text("\u2705 Passou") else Text("\u274c Ficou")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCards(state: HistoryUiState.Success) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Treinos", "${state.totalWorkouts}", Modifier.weight(1f))
        StatCard("Streak M\u00e1x.", "${state.maxStreak}", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
