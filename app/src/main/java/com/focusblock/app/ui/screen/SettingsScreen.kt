package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.focusblock.app.ui.theme.BlockingInactiveColor
import com.focusblock.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToPermissions: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refreshPermissions() }

    if (uiState.showStrictModeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStrictModeDialog() },
            icon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Enable Strict Mode?") },
            text = { Text("Strict Mode prevents you from manually ending a focus session early.\n\nNote: A determined user can still bypass blocking by disabling permissions in Android Settings or force-stopping this app.") },
            confirmButton = { Button(onClick = { viewModel.confirmStrictMode() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Enable Strict Mode") } },
            dismissButton = { TextButton(onClick = { viewModel.dismissStrictModeDialog() }) { Text("Cancel") } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("BLOCKING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Strict Mode", fontWeight = FontWeight.SemiBold)
                            Text("Disables the Stop button during a session.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = uiState.isStrictMode, onCheckedChange = { viewModel.onStrictModeToggled() })
                    }
                }
            }
            item {
                Text("PERMISSIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToPermissions) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (uiState.permissionStatus.hasMinimumRequired) Icons.Filled.CheckCircle else Icons.Filled.Warning, null,
                            tint = if (uiState.permissionStatus.hasMinimumRequired) BlockingInactiveColor else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manage Permissions", fontWeight = FontWeight.SemiBold)
                            Text(if (uiState.permissionStatus.allGranted) "All permissions granted" else if (uiState.permissionStatus.hasMinimumRequired) "Some optional permissions missing" else "Required permissions missing!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, null)
                    }
                }
            }
            item {
                Text("PRIVACY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary); Spacer(modifier = Modifier.width(12.dp)); Text("Privacy", fontWeight = FontWeight.SemiBold) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("FocusBlock works completely offline. No data leaves your device. No user accounts, analytics, or advertising SDKs. All data stays on your device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("FocusBlock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("A standalone productivity tool for temporarily blocking distracting apps. Built for direct APK distribution.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("\u26a0 Android Limitations: FocusBlock uses official Android APIs. Blocking can be bypassed by disabling permissions, force-stopping this app, rebooting, or uninstalling FocusBlock. This app is designed for self-discipline, not absolute security enforcement.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
