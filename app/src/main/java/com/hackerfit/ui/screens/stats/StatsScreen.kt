package com.hackerfit.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.constants.FitnessLadder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    innerPadding: PaddingValues,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableStateOf<DayActivity?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estat\u00edsticas", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is StatsUiState.Loading -> {
                    item(key = "loading") {
                        Box(
                            Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is StatsUiState.Success -> {
                    item(key = "cards_grid") { StatsCardsGrid(state) }
                    item(key = "weekly_activity") {
                        WeeklyActivityRow(
                            weeklyData = state.weeklyData,
                            onDayClick = { day ->
                                if (day.rung != null) {
                                    selectedDay = day
                                }
                            }
                        )
                    }
                    if (state.rungProgression.isNotEmpty()) {
                        item(key = "rung_header") {
                            Text(
                                text = "Progress\u00e3o de Degrau",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(state.rungProgression, key = { "rung_${it.id}" }) { entry ->
                            RungProgressCard(entry)
                        }
                    }
                }
            }
            item(key = "spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (selectedDay != null) {
        val day = selectedDay!!
        ModalBottomSheet(
            onDismissRequest = { selectedDay = null },
            sheetState = bottomSheetState
        ) {
            DaySummaryContent(day)
        }
    }
}

@Composable
private fun DaySummaryContent(day: DayActivity) {
    val rung = day.rung ?: return
    val rungData = FitnessLadder.getRung(rung)
    val isIntroductory = FitnessLadder.isIntroductory(rung)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = day.date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, EEEE", Locale("pt", "BR"))),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Degrau $rung - ${if (isIntroductory) "Introdut\u00f3ria" else "Vital\u00edcia"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (day.completed) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (day.completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (day.completed) "Conclu\u00eddo" else "N\u00e3o realizado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (day.completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Exerc\u00edcios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        FitnessLadder.exercises.forEachIndexed { idx, exercise ->
            val target = when (idx) {
                0 -> rungData.bend
                1 -> rungData.sitUp
                2 -> rungData.legLift
                3 -> rungData.pushUp
                4 -> rungData.runJumpSets
                else -> 0
            }
            val targetText = if (idx == 4) {
                "$target sets + ${rungData.runJumpExtraSteps} passos"
            } else {
                "$target repetic${if (target != 1) "oes" else "ao"}"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (idx < FitnessLadder.exercises.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun StatsCardsGrid(state: StatsUiState.Success) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardItem(
                label = "Melhor Streak",
                value = "${state.bestStreak}",
                icon = Icons.Filled.EmojiEvents,
                modifier = Modifier.weight(1f)
            )
            StatCardItem(
                label = "Degrau Atual",
                value = "${state.currentRung}",
                icon = Icons.Filled.Stairs,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCardItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityRow(
    weeklyData: List<DayActivity>,
    onDayClick: (DayActivity) -> Unit
) {
    val today = LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Atividade Semanal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyData.forEach { day ->
                    val isToday = day.date == today
                    val hasLog = day.rung != null
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.completed) MaterialTheme.colorScheme.tertiary
                                    else if (day.rung != null) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.surface
                                )
                                .then(
                                    if (isToday && !day.completed) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                                .clickable(enabled = hasLog) { onDayClick(day) }
                        ) {
                            if (day.rung != null) {
                                Text(
                                    text = "${day.rung}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (day.completed) MaterialTheme.colorScheme.onTertiary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (day.completed) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RungProgressCard(entry: RungProgressEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Degrau ${entry.fromRung}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "\u2192",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Degrau ${entry.toRung}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
