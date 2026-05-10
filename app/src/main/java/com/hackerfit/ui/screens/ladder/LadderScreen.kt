package com.hackerfit.ui.screens.ladder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.domain.constants.RungData
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderScreen(
    onBack: () -> Unit,
    viewModel: LadderViewModel = hiltViewModel()
) {
    val currentRung by viewModel.currentRung.collectAsState()
    val introRungs = FitnessLadder.introductoryLadder
    val lifetimeRungs = FitnessLadder.lifetimeLadder

    val listState = rememberLazyListState()
    val currentRungIndex = if (currentRung <= 15) currentRung - 1 else currentRung - 16
    val initialIndex = if (currentRung <= 15) currentRung - 1 else 15 + (currentRung - 16)

    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex.coerceAtLeast(0))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escada Completa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item(key = "intro_header") {
                SectionHeader("Escada Introdut\u00f3ria (1-15)")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(introRungs, key = { "intro_${it.number}" }) { rung ->
                RungCard(
                    rungData = rung,
                    isCurrent = rung.number == currentRung,
                    isCompleted = rung.number < currentRung,
                    isLocked = rung.number > currentRung
                )
            }
            item(key = "lifetime_header") {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Escada Vital\u00edcia (16-48)")
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(lifetimeRungs, key = { "lifetime_${it.number}" }) { rung ->
                RungCard(
                    rungData = rung,
                    isCurrent = rung.number == currentRung,
                    isCompleted = rung.number < currentRung,
                    isLocked = rung.number > currentRung
                )
            }
            item(key = "spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun RungCard(
    rungData: RungData,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isLocked: Boolean
) {
    var expanded by remember { mutableStateOf(isCurrent) }

    val cardColors = when {
        isCurrent -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        isCompleted -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
        else -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = cardColors,
        border = if (isCurrent) CardDefaults.outlinedCardBorder()
            else if (isLocked) CardDefaults.outlinedCardBorder()
            else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.tertiary
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    ) {
                        when {
                            isCurrent -> Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            isCompleted -> Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            isLocked -> Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            else -> Text(
                                text = "${rungData.number}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Degrau ${rungData.number}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = summarizeExercises(rungData),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isCurrent) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Atual", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            labelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ExerciseRow("Flex\u00e3o para Frente", rungData.bend, "repeti\u00e7\u00f5es")
                    ExerciseRow("Abdominal", rungData.sitUp, "repeti\u00e7\u00f5es")
                    ExerciseRow("Eleva\u00e7\u00e3o de Pernas", rungData.legLift, "repeti\u00e7\u00f5es")
                    ExerciseRow("Flex\u00e3o de Bra\u00e7o", rungData.pushUp, "repeti\u00e7\u00f5es")
                    ExerciseRow(
                        "Corrida e Salto",
                        rungData.runJumpSets,
                        "sets + ${rungData.runJumpExtraSteps} passos"
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(name: String, value: Int, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun summarizeExercises(rung: RungData): String {
    return "${rung.bend} | ${rung.sitUp} | ${rung.legLift} | ${rung.pushUp} | ${rung.runJumpSets}s"
}
