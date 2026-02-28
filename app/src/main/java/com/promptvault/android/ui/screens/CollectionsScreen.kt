package com.promptvault.android.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.promptvault.android.ui.components.PromptCardData
import java.time.Instant

/**
 * CollectionsScreen - User's prompt collections
 *
 * Reference: TIMELINE.md Milestone 2.6 (Collections)
 * Reference: XENOCOGNITIVE_IDEAS.md (Organization feature supporting hormesis principle)
 *
 * Organization feature supporting hormesis principle from PRESSURE_POINTS.md
 * Collections allow users to organize and group related prompts for quick access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onCollectionClick: (Long) -> Unit = {},
    onCreateCollection: () -> Unit = {},
    onFabClick: () -> Unit = onCreateCollection
) {
    val mockCollections = generateMockCollections()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCollectionId by remember { mutableStateOf<Long?>(null) }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, color ->
                // TODO: Create collection in repository
                showCreateDialog = false
            }
        )
    }

    if (selectedCollectionId != null) {
        CollectionDetailView(
            collectionId = selectedCollectionId!!,
            onBackClick = { selectedCollectionId = null },
            onBulkAdd = { /* TODO: Bulk add prompts */ }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Collections") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create collection"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create collection"
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mockCollections.isEmpty()) {
                    item {
                        EmptyCollectionsPlaceholder(onCreateClick = { showCreateDialog = true })
                    }
                } else {
                    items(mockCollections, key = { it.id }) { collection ->
                        CollectionListItem(
                            collection = collection,
                            onClick = {
                                selectedCollectionId = collection.id
                                onCollectionClick(collection.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionListItem(
    collection: CollectionModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Center
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = parseColorHex(collection.color),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            // Collection info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${collection.promptCount} prompts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (collection.description.isNotEmpty()) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View collection",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EmptyCollectionsPlaceholder(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = "No collections",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No Collections Yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Create your first collection to organize your prompts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onCreateClick) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Collection")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }

    val colors = listOf(
        "#FF6200EE", // Purple
        "#FF03DAC6", // Teal
        "#FFFF6D00", // Orange
        "#FFF62F7C", // Pink
        "#FF3700B3", // Dark Blue
        "#FF00BCD4"  // Cyan
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Collection") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Collection name") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Description (optional)") },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Color picker
                Text(
                    text = "Collection Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEachIndexed { index, color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = parseColorHex(color),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedColorIndex = index }
                                .then(
                                    if (selectedColorIndex == index) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onCreate(name, colors[selectedColorIndex])
                    }
                },
                enabled = name.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetailView(
    collectionId: Long,
    onBackClick: () -> Unit,
    onBulkAdd: () -> Unit
) {
    val collection = generateMockCollection(collectionId)
    var showBulkAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBulkAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add prompts"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(collection.prompts, key = { it.id }) { prompt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* TODO: Open prompt detail */ },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = prompt.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = prompt.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = prompt.complexity,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBulkAddDialog) {
        BulkAddPromptsDialog(
            collectionId = collectionId,
            onDismiss = { showBulkAddDialog = false },
            onAdd = { /* TODO: Add selected prompts */ }
        )
    }
}

@Composable
private fun BulkAddPromptsDialog(
    collectionId: Long,
    onDismiss: () -> Unit,
    onAdd: (List<Long>) -> Unit
) {
    var selectedPromptIds by remember { mutableStateOf(setOf<Long>()) }
    val mockPrompts = generateMockBulkPrompts()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Prompts to Collection") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mockPrompts, key = { it.id }) { prompt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPromptIds = if (selectedPromptIds.contains(prompt.id)) {
                                    selectedPromptIds - prompt.id
                                } else {
                                    selectedPromptIds + prompt.id
                                }
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedPromptIds.contains(prompt.id),
                            onCheckedChange = {
                                selectedPromptIds = if (selectedPromptIds.contains(prompt.id)) {
                                    selectedPromptIds - prompt.id
                                } else {
                                    selectedPromptIds + prompt.id
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prompt.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = prompt.preview,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedPromptIds.toList()) },
                enabled = selectedPromptIds.isNotEmpty()
            ) {
                Text("Add ${selectedPromptIds.size}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data models
data class CollectionModel(
    val id: Long,
    val name: String,
    val description: String,
    val color: String,
    val promptCount: Int,
    val prompts: List<PromptCardData> = emptyList(),
    val createdAt: Instant = Instant.now()
)

// Mock data generators
private fun generateMockCollections(): List<CollectionModel> {
    val colors = listOf(
        "#FF6200EE", // Purple
        "#FF03DAC6", // Teal
        "#FFFF6D00", // Orange
        "#FFF62F7C", // Pink
        "#FF3700B3", // Dark Blue
        "#FF00BCD4"  // Cyan
    )

    return (1..5).map { i ->
        CollectionModel(
            id = i.toLong(),
            name = "Collection $i",
            description = "A collection for organizing related prompts",
            color = colors[i % colors.size],
            promptCount = (i * 3) + 2
        )
    }
}

private fun generateMockCollection(id: Long): CollectionModel {
    val prompts = (1..8).map { i ->
        PromptCardData(
            id = (id * 100) + i,
            title = "Prompt ${id}-${i}",
            preview = "This is a preview of prompt ${id}-${i}. Click to view full content.",
            isFavorite = i % 3 == 0,
            usageCount = (i * 2),
            complexity = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED")[i % 3],
            tags = listOf("tag$i", "category${i % 2}")
        )
    }

    return CollectionModel(
        id = id,
        name = "Collection $id",
        description = "A collection of related prompts for this topic",
        color = listOf("#FF6200EE", "#FF03DAC6", "#FFFF6D00", "#FFF62F7C")[id.toInt() % 4],
        promptCount = prompts.size,
        prompts = prompts
    )
}

private fun generateMockBulkPrompts(): List<PromptCardData> {
    return (1..15).map { i ->
        PromptCardData(
            id = i.toLong(),
            title = "Prompt $i",
            preview = "Preview of prompt $i",
            complexity = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT")[i % 4]
        )
    }
}

// Helper function to parse color hex strings
private fun parseColorHex(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        androidx.compose.ui.graphics.Color(hex.removePrefix("#").toLong(16).toInt() or 0xFF000000.toInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
}

// Extension for border modifier
private fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.foundation.shape.Shape
): Modifier = this.then(
    androidx.compose.foundation.border(width, color, shape)
)
