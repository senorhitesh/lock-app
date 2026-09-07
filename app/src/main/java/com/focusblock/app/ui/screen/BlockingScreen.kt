package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusblock.app.ui.components.TimerDisplay
import com.focusblock.app.ui.theme.BlockingActiveColor
import com.focusblock.app.ui.viewmodel.BlockingUiState

@Composable
fun BlockingScreen(uiState: BlockingUiState, onGoBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Surface(shape = CircleShape, color = BlockingActiveColor.copy(alpha = 0.15f), modifier = Modifier.size(120.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = BlockingActiveColor, modifier = Modifier.size(60.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Focus Time", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "${uiState.blockedAppName} is currently blocked.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(40.dp))
            if (!uiState.sessionExpired) {
                TimerDisplay(remainingMillis = uiState.remainingMillis, label = "Session ends in")
            } else {
                Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Session Complete!", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = if (uiState.sessionExpired) "This app is now available again." else "This app will be available again when your focus session ends.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedButton(onClick = onGoBack, modifier = Modifier.fillMaxWidth(0.6f)) {
                Icon(Icons.Filled.Home, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go Back")
            }
        }
    }
}
