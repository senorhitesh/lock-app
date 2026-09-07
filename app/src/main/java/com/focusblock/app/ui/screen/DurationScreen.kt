package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusblock.app.FocusBlockApp
import com.focusblock.app.domain.usecase.StartBlockingResult
import com.focusblock.app.domain.usecase.StartBlockingUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private data class QuickDuration(val label: String, val millis: Long)

private val quickDurations = listOf(
    QuickDuration("15 min", TimeUnit.MINUTES.toMillis(15)),
    QuickDuration("30 min", TimeUnit.MINUTES.toMillis(30)),
    QuickDuration("1 hour", TimeUnit.HOURS.toMillis(1)),
    QuickDuration("2 hours", TimeUnit.HOURS.toMillis(2)),
    QuickDuration("4 hours", TimeUnit.HOURS.toMillis(4))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationScreen(onBack: () -> Unit, onStarted: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FocusBlockApp
    val scope = rememberCoroutineScope()

    var selectedQuick by remember { mutableStateOf<QuickDuration?>(quickDurations[1]) }
    var isCustom by remember { mutableStateOf(false) }
    var customHours by remember { mutableIntStateOf(0) }
    var customMinutes by remember { mutableIntStateOf(30) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isStarting by remember { mutableStateOf(false) }

    val selectedDurationMillis: Long = when {
        isCustom -> TimeUnit.HOURS.toMillis(customHours.toLong()) + TimeUnit.MINUTES.toMillis(customMinutes.toLong())
        else -> selectedQuick?.millis ?: 0L
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Duration", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Block selected apps for:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickDurations) { duration ->
                    FilterChip(selected = selectedQuick == duration && !isCustom, onClick = { selectedQuick = duration; isCustom = false }, label = { Text(duration.label) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FilterChip(selected = isCustom, onClick = { isCustom = true; selectedQuick = null }, label = { Text("Custom") }, leadingIcon = { Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp)) })

            if (isCustom) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Hours: $customHours", style = MaterialTheme.typography.labelLarge)
                        Slider(value = customHours.toFloat(), onValueChange = { customHours = it.toInt() }, valueRange = 0f..12f, steps = 11)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Minutes: $customMinutes", style = MaterialTheme.typography.labelLarge)
                        Slider(value = customMinutes.toFloat(), onValueChange = { customMinutes = it.toInt() }, valueRange = 0f..55f, steps = 10)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (selectedDurationMillis > 0) {
                Text(
                    text = if (isCustom) buildString { if (customHours > 0) append("$customHours h "); if (customMinutes > 0) append("$customMinutes min") }.trim().ifEmpty { "Select duration" } else selectedQuick?.label ?: "",
                    style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium); Spacer(modifier = Modifier.height(8.dp)) }

            Button(
                onClick = {
                    if (selectedDurationMillis <= 0) { errorMessage = "Please select a valid duration."; return@Button }
                    scope.launch {
                        isStarting = true
                        val blockedPackages = app.appRepository.getBlockedPackages()
                        val strictMode = app.appPreferences.isStrictMode.first()
                        val result = StartBlockingUseCase(context, app.sessionRepository).execute(blockedPackages, selectedDurationMillis, strictMode)
                        isStarting = false
                        when (result) {
                            StartBlockingResult.Success -> onStarted()
                            StartBlockingResult.NoAppsSelected -> errorMessage = "Please select at least one app to block."
                            StartBlockingResult.InvalidDuration -> errorMessage = "Please select a valid duration."
                            StartBlockingResult.PermissionsRequired -> errorMessage = "Permissions required. Please grant Usage Access or Accessibility."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedDurationMillis > 0 && !isStarting
            ) {
                if (isStarting) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else { Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Start Blocking", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}
