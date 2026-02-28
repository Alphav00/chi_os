package com.promptvault.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.reorderable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.promptvault.android.data.model.QuickLink
import com.promptvault.android.ui.quicklinks.QuickLinksViewModel

/**
 * QuickLinksScreen: User's quick shortcuts to favorite prompts/collections
 *
 * Responsibilities:
 * 1. Display user's quick links (shortcuts)
 * 2. Create new quick link dialog
 * 3. Deep link preview (shows URI)
 * 4. Edit/delete quick links
 * 5. Sort order management (drag-to-reorder)
 *
 * References:
 * - TIMELINE.md Milestone 2.5 (Quick Links & Deep Linking)
 * - DEVOPS.md DeepLinkHandler v2 (Versioned deep links)
 * - DEVOPS_AND_TEAMS.md: Android System Integration Engineer responsibility
 *
 * Comments:
 * - Versioned deep links per DEVOPS.md DeepLinkHandler v2
 */

@Composable
fun QuickLinksScreen(
    modifier: Modifier = Modifier,
    viewModel: QuickLinksViewModel = hiltViewModel()
) {
    val quickLinks by viewModel.quickLinks.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedQuickLink by remember { mutableStateOf<QuickLink?>(null) }

    // Load quick links on first composition
    LaunchedEffect(Unit) {
        viewModel.loadQuickLinks()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Quick Link")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (quickLinks.isEmpty() && !isLoading) {
                EmptyQuickLinksState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Title
                    item {
                        Text(
                            text = "Quick Links",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Subtitle
                    item {
                        Text(
                            text = "Shortcuts to your favorite prompts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Quick links list
                    items(
                        items = quickLinks,
                        key = { it.id }
                    ) { quickLink ->
                        QuickLinkCard(
                            quickLink = quickLink,
                            onEdit = {
                                selectedQuickLink = quickLink
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteQuickLink(quickLink.id)
                            },
                            onMoveUp = {
                                viewModel.reorderQuickLink(quickLink.id, -1)
                            },
                            onMoveDown = {
                                viewModel.reorderQuickLink(quickLink.id, 1)
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Spacer at end
                    item {
                        Box(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Create Quick Link Dialog
    if (showCreateDialog) {
        CreateQuickLinkDialog(
            onConfirm = { name, description ->
                viewModel.createQuickLink(name, description)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit Quick Link Dialog
    if (showEditDialog && selectedQuickLink != null) {
        EditQuickLinkDialog(
            quickLink = selectedQuickLink!!,
            onConfirm = { name, description ->
                viewModel.updateQuickLink(
                    id = selectedQuickLink!!.id,
                    name = name,
                    description = description
                )
                showEditDialog = false
                selectedQuickLink = null
            },
            onDismiss = {
                showEditDialog = false
                selectedQuickLink = null
            }
        )
    }
}

@Composable
private fun QuickLinkCard(
    quickLink: QuickLink,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Could navigate to linked item */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quickLink.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (quickLink.description.isNotEmpty()) {
                        Text(
                            text = quickLink.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Action buttons
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Deep link preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Deep Link URI (v${quickLink.uriVersion})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = quickLink.deepLinkUri,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Order management buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onMoveUp,
                    modifier = Modifier.padding(end = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("↑ Move Up", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onMoveDown,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Move Down ↓", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun EmptyQuickLinksState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Quick Links Yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Create shortcuts to your favorite prompts and collections",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = "Tap the + button to create your first quick link",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreateQuickLinkDialog(
    onConfirm: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Quick Link") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Quick Link Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g., Marketing Template") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = { Text("What is this shortcut for?") },
                    maxLines = 3
                )

                Text(
                    text = "Quick links create direct shortcuts to your prompts and collections",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditQuickLinkDialog(
    quickLink: QuickLink,
    onConfirm: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(quickLink.name) }
    var description by remember { mutableStateOf(quickLink.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Quick Link") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Quick Link Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                Text(
                    text = "Deep Link URI: ${quickLink.deepLinkUri}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
