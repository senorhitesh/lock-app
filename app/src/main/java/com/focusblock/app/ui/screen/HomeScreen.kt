package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusblock.app.ui.components.AppIconImage
import com.focusblock.app.ui.components.TimerDisplay
import com.focusblock.app.ui.theme.BlockingActiveColor
import com.focusblock.app.ui.theme.BlockingInactiveColor
import com.focusblock.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToApps: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDuration: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshPermissions() }

    if (uiState.isStopConfirmVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStopConfirm() },
            title = { Text("Stop Blocking?") },
            text = { Text("Are you sure you want to end the current focus session?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmStop() }) { Text("Stop", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissStopConfirm() }) { Text("Keep Going") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FOCUSBLOCK", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) },
                actions = { IconButton(onClick = onNavigateToHistory) { Icon(Icons.Filled.History, "History") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!uiState.permissionStatus.hasMinimumRequired) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), onClick = onNavigateToPermissions) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Permissions needed", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                Text("Tap to set up required permissions.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                val session = uiState.activeSession
                val isBlocking = session != null
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isBlocking) BlockingActiveColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = if (isBlocking) Icons.Filled.Lock else Icons.Filled.LockOpen, contentDescription = null, tint = if (isBlocking) BlockingActiveColor else BlockingInactiveColor, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isBlocking) "BLOCKING ACTIVE" else "Not Blocking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isBlocking) BlockingActiveColor else BlockingInactiveColor)
                        }
                        if (isBlocking && session != null) {
                            Spacer(modifier = Modifier.height(20.dp))
                            TimerDisplay(remainingMillis = uiState.remainingMillis)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "${uiState.blockedApps.size} apps blocked", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (session.isStrictMode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.errorContainer) {
                                    Text("Strict Mode", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.blockedApps.isNotEmpty()) {
                item {
                    Text("Blocked Apps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.blockedApps) { app ->
                            AssistChip(onClick = {}, label = { Text(app.appName) }, leadingIcon = { AppIconImage(drawable = app.icon, modifier = Modifier.size(20.dp)) })
                        }
                    }
                }
            } else if (uiState.blockedPackages.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("No apps selected. Tap 'Manage Apps' to choose apps to block.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                if (uiState.activeSession != null) {
                    OutlinedButton(
                        onClick = { viewModel.onStopClicked() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (uiState.activeSession?.isStrictMode == true) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error),
                        enabled = uiState.activeSession?.isStrictMode != true
                    ) {
                        Icon(Icons.Filled.Stop, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (uiState.activeSession?.isStrictMode == true) "Strict Mode (Cannot Stop Early)" else "Stop Blocking")
                    }
                } else {
                    Button(
                        onClick = {
                            if (uiState.blockedPackages.isEmpty()) return@Button
                            if (!uiState.permissionStatus.hasMinimumRequired) onNavigateToPermissions()
                            else onNavigateToDuration()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.blockedPackages.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Blocking")
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Apps, label = "Manage Apps", onClick = onNavigateToApps)
                    QuickActionCard(modifier = Modifier.weight(1f), icon = Icons.Filled.Schedule, label = "Schedule", onClick = onNavigateToSchedules)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier = Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
