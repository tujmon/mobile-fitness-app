package com.hackerfit.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.domain.constants.FitnessLadder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface HistoryDialog {
    data object None : HistoryDialog
    data class EditLog(
        val id: Long,
        val date: LocalDate,
        val rung: Int,
        val completed: Boolean
    ) : HistoryDialog

    data class EditAssessment(
        val id: Long,
        val date: LocalDate,
        val fromRung: Int,
        val toRung: Int,
        val passed: Boolean,
        val notes: String?
    ) : HistoryDialog

    data class ConfirmDeleteLog(val id: Long, val date: LocalDate) : HistoryDialog
    data class ConfirmDeleteAssessment(val id: Long, val date: LocalDate) : HistoryDialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    innerPadding: PaddingValues,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    var dialogState by remember { mutableStateOf<HistoryDialog>(HistoryDialog.None) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hist\u00f3rico", fontWeight = FontWeight.Bold) }
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
                is HistoryUiState.Loading -> {
                    item(key = "loading") {
                        Box(
                            Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is HistoryUiState.Success -> {
                    item(key = "calendar") {
                        InteractiveCalendar(
                            yearMonth = state.selectedMonth,
                            completedDates = state.completedDates,
                            logByDate = state.logByDate,
                            onMonthChange = { viewModel.selectMonth(it) },
                            onDayClick = { date ->
                                viewModel.selectDay(date)
                                showBottomSheet = true
                            }
                        )
                    }

                    item(key = "logs_header") {
                        Text(
                            text = "Treinos do M\u00eas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (state.recentLogs.isEmpty()) {
                        item(key = "logs_empty") { EmptyState(message = "Nenhum treino registrado neste m\u00eas.") }
                    } else {
                        items(state.recentLogs, key = { "log_${it.id}" }) { log ->
                            WorkoutLogCard(
                                log = log,
                                onEdit = {
                                    dialogState = HistoryDialog.EditLog(
                                        id = log.id,
                                        date = log.date,
                                        rung = log.rung,
                                        completed = log.completed
                                    )
                                },
                                onDelete = {
                                    dialogState = HistoryDialog.ConfirmDeleteLog(
                                        id = log.id,
                                        date = log.date
                                    )
                                }
                            )
                        }
                    }

                    if (state.assessments.isNotEmpty()) {
                        item(key = "assessments_header") {
                            Text(
                                text = "Avalia\u00e7\u00f5es",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(state.assessments, key = { "assessment_${it.id}" }) { a ->
                            AssessmentCard(
                                assessment = a,
                                onEdit = {
                                    dialogState = HistoryDialog.EditAssessment(
                                        id = a.id,
                                        date = a.date,
                                        fromRung = a.fromRung,
                                        toRung = a.toRung,
                                        passed = a.passed,
                                        notes = a.notes
                                    )
                                },
                                onDelete = {
                                    dialogState = HistoryDialog.ConfirmDeleteAssessment(
                                        id = a.id,
                                        date = a.date
                                    )
                                }
                            )
                        }
                    }
                }
            }
            item(key = "spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    val successState = uiState as? HistoryUiState.Success

    HistoryBottomSheet(
        visible = showBottomSheet && successState?.dayDetail != null,
        detail = successState?.dayDetail,
        sheetState = bottomSheetState,
        onEdit = { detail ->
            dialogState = HistoryDialog.EditLog(
                id = detail.id,
                date = detail.date,
                rung = detail.rung,
                completed = detail.completed
            )
            showBottomSheet = false
        },
        onDelete = { detail ->
            dialogState = HistoryDialog.ConfirmDeleteLog(
                id = detail.id,
                date = detail.date
            )
            showBottomSheet = false
        },
        onDismiss = {
            showBottomSheet = false
            viewModel.selectDay(null)
        }
    )

    HistoryDialogs(
        dialogState = dialogState,
        onConfirmEditLog = { id, date, rung, completed ->
            viewModel.updateLog(id, date, rung, completed)
            dialogState = HistoryDialog.None
        },
        onConfirmEditAssessment = { id, date, fromRung, toRung, passed, notes ->
            viewModel.updateAssessment(id, date, fromRung, toRung, passed, notes)
            dialogState = HistoryDialog.None
        },
        onConfirmDeleteLog = { id ->
            viewModel.deleteLog(id)
            dialogState = HistoryDialog.None
        },
        onConfirmDeleteAssessment = { id ->
            viewModel.deleteAssessment(id)
            dialogState = HistoryDialog.None
        },
        onDismiss = { dialogState = HistoryDialog.None }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryBottomSheet(
    visible: Boolean,
    detail: HistoryUiState.DayDetail?,
    sheetState: SheetState,
    onEdit: (HistoryUiState.DayDetail) -> Unit,
    onDelete: (HistoryUiState.DayDetail) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible && detail != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            DayDetailContent(
                detail = detail,
                onEdit = { onEdit(detail) },
                onDelete = { onDelete(detail) }
            )
        }
    }
}

@Composable
private fun HistoryDialogs(
    dialogState: HistoryDialog,
    onConfirmEditLog: (Long, LocalDate, Int, Boolean) -> Unit,
    onConfirmEditAssessment: (Long, LocalDate, Int, Int, Boolean, String?) -> Unit,
    onConfirmDeleteLog: (Long) -> Unit,
    onConfirmDeleteAssessment: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    when (val dialog = dialogState) {
        is HistoryDialog.EditLog -> {
            EditLogDialog(
                initialRung = dialog.rung,
                initialCompleted = dialog.completed,
                date = dialog.date,
                onConfirm = { rung, completed ->
                    onConfirmEditLog(dialog.id, dialog.date, rung, completed)
                },
                onDismiss = onDismiss
            )
        }
        is HistoryDialog.EditAssessment -> {
            EditAssessmentDialog(
                initialFromRung = dialog.fromRung,
                initialToRung = dialog.toRung,
                initialPassed = dialog.passed,
                initialNotes = dialog.notes,
                date = dialog.date,
                onConfirm = { fromRung, toRung, passed, notes ->
                    onConfirmEditAssessment(dialog.id, dialog.date, fromRung, toRung, passed, notes)
                },
                onDismiss = onDismiss
            )
        }
        is HistoryDialog.ConfirmDeleteLog -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Excluir treino?") },
                text = {
                    Text("Excluir o treino de ${dialog.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}? Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirmDeleteLog(dialog.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                }
            )
        }
        is HistoryDialog.ConfirmDeleteAssessment -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Excluir avalia\u00e7\u00e3o?") },
                text = {
                    Text("Excluir a avalia\u00e7\u00e3o de ${dialog.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}? Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirmDeleteAssessment(dialog.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                }
            )
        }
        HistoryDialog.None -> {}
    }
}

@Composable
private fun EditLogDialog(
    initialRung: Int,
    initialCompleted: Boolean,
    date: LocalDate,
    onConfirm: (rung: Int, completed: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var rung by remember { mutableIntStateOf(initialRung) }
    var completed by remember { mutableStateOf(initialCompleted) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Treino") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, EEEE", Locale("pt", "BR"))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Degrau", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (rung > 1) rung-- },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("\u2212") }
                        Text(
                            text = "$rung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 32.dp),
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { if (rung < 48) rung++ },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("+") }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Conclu\u00eddo", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = completed, onCheckedChange = { completed = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rung, completed) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditAssessmentDialog(
    initialFromRung: Int,
    initialToRung: Int,
    initialPassed: Boolean,
    initialNotes: String?,
    date: LocalDate,
    onConfirm: (fromRung: Int, toRung: Int, passed: Boolean, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var fromRung by remember { mutableIntStateOf(initialFromRung) }
    var toRung by remember { mutableIntStateOf(initialToRung) }
    var passed by remember { mutableStateOf(initialPassed) }
    var notes by remember { mutableStateOf(initialNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Avalia\u00e7\u00e3o") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, EEEE", Locale("pt", "BR"))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Degrau de origem", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (fromRung > 1) fromRung-- },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("\u2212") }
                        Text(
                            text = "$fromRung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 28.dp),
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { if (fromRung < 48) fromRung++ },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("+") }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Degrau de destino", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (toRung > 1) toRung-- },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("\u2212") }
                        Text(
                            text = "$toRung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 28.dp),
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { if (toRung < 48) toRung++ },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("+") }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Passou", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = passed, onCheckedChange = { passed = it })
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(fromRung, toRung, passed, notes.ifBlank { null }) }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun InteractiveCalendar(
    yearMonth: YearMonth,
    completedDates: Set<LocalDate>,
    logByDate: Map<LocalDate, HistoryUiState.RecentLog>,
    onMonthChange: (YearMonth) -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val today = LocalDate.now()
    val isCurrentMonth = yearMonth == YearMonth.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(yearMonth.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "M\u00eas anterior"
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isCurrentMonth) {
                        TextButton(
                            onClick = { onMonthChange(YearMonth.now()) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Hoje",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                IconButton(onClick = { onMonthChange(yearMonth.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Pr\u00f3ximo m\u00eas"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (dayNumber in 1..daysInMonth) {
                                val date = yearMonth.atDay(dayNumber)
                                val isCompleted = completedDates.contains(date)
                                val hasLog = logByDate.containsKey(date)
                                val isToday = date == today
                                val isFuture = date > today

                                val bgColor = when {
                                    isCompleted -> MaterialTheme.colorScheme.tertiary
                                    hasLog -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val textColor = when {
                                    isCompleted -> MaterialTheme.colorScheme.onTertiary
                                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .then(
                                            if (isToday && !isCompleted) {
                                                Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                            } else Modifier
                                        )
                                        .clickable(
                                            enabled = !isFuture && hasLog
                                        ) { onDayClick(date) }
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor
                                    )
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
private fun DayDetailContent(
    detail: HistoryUiState.DayDetail,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rungData = FitnessLadder.getRung(detail.rung)
    val isIntroductory = FitnessLadder.isIntroductory(detail.rung)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = detail.date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, EEEE", Locale("pt", "BR"))),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Degrau ${detail.rung} - ${if (isIntroductory) "Introdut\u00f3ria" else "Vital\u00edcia"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (detail.completed) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (detail.completed) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (detail.completed) "Conclu\u00eddo" else "N\u00e3o realizado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (detail.completed) MaterialTheme.colorScheme.primary
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Editar")
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Excluir")
            }
        }
    }
}

@Composable
private fun WorkoutLogCard(
    log: HistoryUiState.RecentLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = log.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Degrau ${log.rung}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (log.completed) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = if (log.completed) "Conclu\u00eddo" else "N\u00e3o realizado",
                    tint = if (log.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Op\u00e7\u00f5es",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AssessmentCard(
    assessment: HistoryUiState.AssessmentEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = assessment.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Degrau ${assessment.fromRung}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "\u2192",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Degrau ${assessment.toRung}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (assessment.passed) Icons.Filled.EmojiEvents else Icons.Filled.Close,
                        contentDescription = null,
                        tint = if (assessment.passed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (assessment.passed) "Passou" else "Ficou",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Op\u00e7\u00f5es",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
