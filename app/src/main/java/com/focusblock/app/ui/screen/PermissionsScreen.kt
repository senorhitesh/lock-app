package com.focusblock.app.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusblock.app.domain.usecase.CheckPermissionsUseCase
import com.focusblock.app.domain.usecase.PermissionStatus
import com.focusblock.app.ui.theme.BlockingActiveColor
import com.focusblock.app.ui.theme.BlockingInactiveColor
import com.focusblock.app.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(CheckPermissionsUseCase(context).execute()) }

    val notifLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            status = CheckPermissionsUseCase(context).execute()
        }
    } else null

    LaunchedEffect(Unit) { status = CheckPermissionsUseCase(context).execute() }

    Scaffold(topBar = { TopAppBar(title = { Text("Required Permissions", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("FocusBlock needs these permissions to reliably detect and block apps.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                PermissionCard(icon = Icons.Filled.Analytics, title = "Usage Access", description = "Detects which app is in the foreground using Android's UsageStats API.", isGranted = status.hasUsageAccess, onGrant = { PermissionUtils.openUsageAccessSettings(context); status = CheckPermissionsUseCase(context).execute() })
            }
            item {
                PermissionCard(icon = Icons.Filled.Accessibility, title = "Accessibility Access", description = "Instantly detects when a blocked app is opened (most reliable method).", isGranted = status.hasAccessibility, onGrant = { PermissionUtils.openAccessibilitySettings(context) })
            }
            item {
                PermissionCard(icon = Icons.Filled.Notifications, title = "Notifications", description = "Shows a persistent notification during a blocking session.", isGranted = status.hasNotifications, onGrant = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else PermissionUtils.openNotificationSettings(context)
                })
            }
            item {
                PermissionCard(icon = Icons.Filled.BatteryChargingFull, title = "Battery Optimization", description = "Prevents Android from stopping FocusBlock in the background.", isGranted = status.hasBatteryOptimizationExemption, onGrant = { PermissionUtils.openBatteryOptimizationSettings(context) })
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Important Note", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("FocusBlock uses official Android APIs. A determined user can bypass blocking by disabling permissions or force-stopping this app. FocusBlock is designed for self-discipline, not security enforcement.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), enabled = status.hasMinimumRequired) { Text("Continue") }
                if (!status.hasMinimumRequired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Grant at least Usage Access OR Accessibility Access to continue.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, title: String, description: String, isGranted: Boolean, onGrant: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isGranted) BlockingInactiveColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = if (isGranted) BlockingInactiveColor else MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isGranted) Icon(Icons.Filled.CheckCircle, null, tint = BlockingInactiveColor, modifier = Modifier.size(16.dp))
                    else Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!isGranted) FilledTonalButton(onClick = onGrant, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text("Fix", style = MaterialTheme.typography.labelMedium) }
        }
    }
}
