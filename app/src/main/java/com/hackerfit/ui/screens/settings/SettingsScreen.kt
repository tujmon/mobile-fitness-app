package com.hackerfit.ui.screens.settings

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hackerfit.ui.navigation.MainViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    val pendingImportUri by mainViewModel.pendingImportUri.collectAsStateWithLifecycle()
    LaunchedEffect(pendingImportUri) {
        if (pendingImportUri != null) {
            mainViewModel.clearPendingImportUri()
            viewModel.readImportData(Uri.parse(pendingImportUri))
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.readImportData(it) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val state = uiState as? SettingsUiState.Success
            if (state != null) {
                viewModel.setReminderTime(state.hour, state.minute)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configura\u00e7\u00f5es", fontWeight = FontWeight.Bold) })
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
                is SettingsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SettingsUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Lembrete Di\u00e1rio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.reminderEnabled) {
                                        "Hor\u00e1rio: ${String.format("%02d:%02d", state.hour, state.minute)}"
                                    } else {
                                        "Desativado"
                                    }
                                )
                                Switch(
                                    checked = state.reminderEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            showTimePicker = true
                                        } else {
                                            viewModel.disableReminder()
                                        }
                                    }
                                )
                            }
                            if (state.reminderEnabled) {
                                OutlinedButton(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Alterar Hor\u00e1rio")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Dados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    exportLauncher.launch("hackerfit-backup.json")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Exportar Dados")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    importLauncher.launch(arrayOf("application/json"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importar Dados")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Exporte seus dados para n\u00e3o perder o progresso ao trocar de dispositivo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Apagar Todos os Dados")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "HackerFit v1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Baseado em \"The Hacker's Diet\" por John Walker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        val initialHour = (uiState as? SettingsUiState.Success)?.hour ?: 8
        val initialMinute = (uiState as? SettingsUiState.Success)?.minute ?: 0

        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Hor\u00e1rio do Lembrete") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showTimePicker = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar todos os dados?") },
            text = { Text("Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita. Exporte seus dados antes se deseja manter seu progresso.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    when (val impState = importState) {
        is ImportState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Importando...") },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Lendo dados do arquivo...")
                    }
                },
                confirmButton = {}
            )
        }
        is ImportState.Confirm -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetImportState() },
                title = { Text("Importar dados?") },
                text = {
                    Column {
                        Text("Foram encontrados:")
                        Text("\u2022 ${impState.logCount} registros de treino")
                        Text("\u2022 ${impState.assessmentCount} avaliacoes")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Como deseja importar?")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.confirmImport(replace = true)
                    }) {
                        Text("Substituir tudo")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.confirmImport(replace = false)
                        }) {
                            Text("Mesclar")
                        }
                        TextButton(onClick = { viewModel.resetImportState() }) {
                            Text("Cancelar")
                        }
                    }
                }
            )
        }
        is ImportState.Done -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetImportState() },
                title = { Text("Sucesso") },
                text = { Text("Dados importados com sucesso!") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetImportState() }) {
                        Text("OK")
                    }
                }
            )
        }
        is ImportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetImportState() },
                title = { Text("Erro ao importar") },
                text = { Text(impState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetImportState() }) {
                        Text("OK")
                    }
                }
            )
        }
        ImportState.Idle -> {}
    }

    when (val expState = exportState) {
        is ExportState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Exportando...") },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Salvando dados...")
                    }
                },
                confirmButton = {}
            )
        }
        is ExportState.Done -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetExportState() },
                title = { Text("Sucesso") },
                text = { Text("Dados exportados com sucesso!") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetExportState() }) {
                        Text("OK")
                    }
                }
            )
        }
        is ExportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetExportState() },
                title = { Text("Erro ao exportar") },
                text = { Text(expState.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetExportState() }) {
                        Text("OK")
                    }
                }
            )
        }
        ExportState.Idle -> {}
    }
}
