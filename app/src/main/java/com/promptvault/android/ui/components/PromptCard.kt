package com.promptvault.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Data class for PromptCard
data class PromptCardData(
    val id: Long,
    val title: String,
    val preview: String, // First 100 characters
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val lastUsed: Instant? = null,
    val tags: List<String> = emptyList(),
    val complexity: String = "INTERMEDIATE" // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
)

@Composable
fun PromptCard(
    data: PromptCardData,
    onCardClick: (Long) -> Unit = {},
    onFavoriteToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onLongPress: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(data.isFavorite) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick(data.id) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header with title and favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = data.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            onFavoriteToggle(data.id, isFavorite)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = !showMenu },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { showMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preview text
            Text(
                text = data.preview,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Usage badge
                if (data.usageCount > 0) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Used ${data.usageCount}x",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Complexity badge
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = data.complexity,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )

                // Last used info
                if (data.lastUsed != null) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = formatLastUsed(data.lastUsed),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            // Tags
            if (data.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    data.tags.forEach { tag ->
                        ElevatedAssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = tag,
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

@Composable
fun rememberScrollState() = remember { androidx.compose.foundation.ScrollState(0) }

private fun formatLastUsed(instant: Instant): String {
    val now = Instant.now()
    val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(instant, now).toInt()
    return when {
        daysAgo == 0 -> "Today"
        daysAgo == 1 -> "Yesterday"
        daysAgo < 7 -> "$daysAgo days ago"
        daysAgo < 30 -> "${daysAgo / 7} weeks ago"
        else -> "${daysAgo / 30} months ago"
    }
}

// Preview for Compose preview
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PromptCardPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            PromptCard(
                data = PromptCardData(
                    id = 1L,
                    title = "System Prompt v1",
                    preview = "You are a helpful AI assistant. Your job is to answer questions accurately and concisely.",
                    isFavorite = true,
                    usageCount = 5,
                    tags = listOf("system", "ai", "assistant"),
                    complexity = "INTERMEDIATE"
                )
            )
        }
    }
}
