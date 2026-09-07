package com.focusblock.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class OnboardingPage(val title: String, val description: String, val icon: ImageVector)

private val onboardingPages = listOf(
    OnboardingPage("Welcome to FocusBlock", "Take control of your screen time and boost your productivity by temporarily blocking distracting apps.", Icons.Filled.Lock),
    OnboardingPage("Select Apps to Block", "Choose which apps you want to block during focus sessions — Instagram, YouTube, or any app on your device.", Icons.Filled.Apps),
    OnboardingPage("Set a Duration or Schedule", "Block apps for 15 minutes, 2 hours, or create recurring schedules. FocusBlock keeps running even after you close the app.", Icons.Filled.Schedule),
    OnboardingPage("Stay Focused", "When you try to open a blocked app, FocusBlock shows a reminder screen. Apps become available again automatically when the session ends.", Icons.Filled.CheckCircle)
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = onboardingPages[currentPage]
    val isLastPage = currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(120.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = page.icon, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = page.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = page.description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            onboardingPages.indices.forEach { index ->
                val color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Surface(shape = CircleShape, color = color, modifier = Modifier.size(if (index == currentPage) 10.dp else 8.dp)) {}
                if (index < onboardingPages.lastIndex) Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            if (!isLastPage) TextButton(onClick = onComplete) { Text("Skip") }
            else Spacer(modifier = Modifier.width(64.dp))

            Button(onClick = { if (isLastPage) onComplete() else currentPage++ }, modifier = Modifier.height(48.dp)) {
                Text(if (isLastPage) "Get Started" else "Next")
                if (!isLastPage) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(16.dp)) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
