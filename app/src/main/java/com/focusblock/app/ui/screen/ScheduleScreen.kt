package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusblock.app.data.model.Schedule
import com.focusblock.app.ui.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit, viewModel: ScheduleViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, days, startH, startM, endH, endM ->
                viewModel.addSchedule(name, days, startH, startM, endH, endM, emptySet())
                showAddDialog = false
            }
        )
    }

    scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Delete Schedule") },
            text = { Text("Delete \"${schedule.name}\"?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteSchedule(schedule); scheduleToDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { scheduleToDelete = null }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Schedules", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Filled.Add, "Add") } }
    ) { padding ->
        if (uiState.schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No schedules yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap + to add a blocking schedule", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.schedules, key = { it.id }) { schedule ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(schedule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(schedule.getTimeDisplay(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Text(schedule.getDaysDisplay(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = schedule.isEnabled, onCheckedChange = { viewModel.toggleSchedule(schedule) })
                            IconButton(onClick = { scheduleToDelete = schedule }) { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun AddScheduleDialog(onDismiss: () -> Unit, onSave: (String, Int, Int, Int, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("Focus Session") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(0) }
    var selectedDays by remember { mutableIntStateOf(Schedule.EVERY_DAY) }
    val dayItems = listOf("Sun" to Schedule.SUNDAY, "Mon" to Schedule.MONDAY, "Tue" to Schedule.TUESDAY, "Wed" to Schedule.WEDNESDAY, "Thu" to Schedule.THURSDAY, "Fri" to Schedule.FRIDAY, "Sat" to Schedule.SATURDAY)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Days", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    dayItems.forEach { (label, bit) ->
                        val selected = selectedDays and bit != 0
                        FilterChip(selected = selected, onClick = { selectedDays = if (selected) selectedDays and bit.inv() else selectedDays or bit }, label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { startHour = (startHour - 1 + 24) % 24 }) { Icon(Icons.Filled.Remove, null) }
                            Text("%02d:%02d".format(startHour, startMinute))
                            IconButton(onClick = { startHour = (startHour + 1) % 24 }) { Icon(Icons.Filled.Add, null) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { startMinute = (startMinute - 15 + 60) % 60 }) { Icon(Icons.Filled.Remove, null) }
                            Text("${startMinute}m")
                            IconButton(onClick = { startMinute = (startMinute + 15) % 60 }) { Icon(Icons.Filled.Add, null) }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { endHour = (endHour - 1 + 24) % 24 }) { Icon(Icons.Filled.Remove, null) }
                            Text("%02d:%02d".format(endHour, endMinute))
                            IconButton(onClick = { endHour = (endHour + 1) % 24 }) { Icon(Icons.Filled.Add, null) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { endMinute = (endMinute - 15 + 60) % 60 }) { Icon(Icons.Filled.Remove, null) }
                            Text("${endMinute}m")
                            IconButton(onClick = { endMinute = (endMinute + 15) % 60 }) { Icon(Icons.Filled.Add, null) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, selectedDays, startHour, startMinute, endHour, endMinute) }, enabled = name.isNotBlank() && selectedDays != 0) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
