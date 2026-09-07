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
import com.focusblock.app.ui.components.AppIconImage
import com.focusblock.app.ui.viewmodel.AppSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(onBack: () -> Unit, viewModel: AppSelectionViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) { viewModel.resetSaved(); onBack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apps to Block", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = { viewModel.selectAll() }) { Text("All") }
                    TextButton(onClick = { viewModel.clearAll() }) { Text("Clear") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { viewModel.saveSelection() }, icon = { Icon(Icons.Filled.Save, null) }, text = { Text("Save (${uiState.selectedPackages.size})") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search apps…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (uiState.searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.onSearchQueryChanged("") }) { Icon(Icons.Filled.Clear, null) } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.filteredApps.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No apps found", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconImage(drawable = app.icon, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.appName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = uiState.selectedPackages.contains(app.packageName), onCheckedChange = { viewModel.onAppToggled(app.packageName) })
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }
    }
}
