package com.promptvault.android.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * DetailScreen - Full prompt view with edit capabilities
 *
 * Reference: TIMELINE.md Milestone 2.2 (Detail Screen)
 * Reference: XENOCOGNITIVE_IDEAS.md #1 (Prompt Genealogy)
 *
 * XENOCOGNITIVE: Genealogy view shows prompt merge history as timeline
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    promptId: Long,
    onBackClick: () -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {}
) {
    val mockPrompt = generateMockPromptDetail(promptId)
    var isEditMode by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(mockPrompt.title) }
    var content by remember { mutableStateOf(mockPrompt.content) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Prompt" else "Prompt Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit prompt"
                            )
                        }
                    } else {
                        IconButton(onClick = { isEditMode = false }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save changes"
                            )
                        }
                    }
                    IconButton(onClick = { onDeleteClick(promptId) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete prompt",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Title section
            item {
                if (isEditMode) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Prompt title") },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Metadata section (created, updated, usage count, complexity)
            item {
                MetadataSection(
                    createdAt = mockPrompt.createdAt,
                    updatedAt = mockPrompt.updatedAt,
                    usageCount = mockPrompt.usageCount,
                    complexity = mockPrompt.complexity,
                    lastUsed = mockPrompt.lastUsed
                )
            }

            // Content section (full text, editable)
            item {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditMode) {
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        placeholder = { Text("Prompt content") },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = content,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Merge history timeline (Genealogy view)
            item {
                MergeHistorySection(mergeHistory = mockPrompt.mergeHistory)
            }

            // Action buttons (Copy, Share, Export)
            item {
                ActionButtonsSection(
                    onCopy = { /* TODO: Copy to clipboard */ },
                    onShare = { /* TODO: Share via intent */ },
                    onExport = { /* TODO: Export prompt */ }
                )
            }

            // Related prompts (serendipitous recommendations)
            item {
                RelatedPromptsSection(relatedPrompts = mockPrompt.relatedPrompts)
            }

            // Spacer at bottom
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MetadataSection(
    createdAt: Instant,
    updatedAt: Instant,
    usageCount: Int,
    complexity: String,
    lastUsed: Instant?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetadataRow(
                label = "Created",
                value = formatInstant(createdAt),
                icon = Icons.Default.DateRange
            )
            MetadataRow(
                label = "Updated",
                value = formatInstant(updatedAt),
                icon = Icons.Default.Update
            )
            MetadataRow(
                label = "Usage Count",
                value = "$usageCount uses",
                icon = Icons.Default.Info
            )
            MetadataRow(
                label = "Complexity",
                value = complexity,
                icon = Icons.Default.TrendingUp
            )
            if (lastUsed != null) {
                MetadataRow(
                    label = "Last Used",
                    value = formatInstant(lastUsed),
                    icon = Icons.Default.Schedule
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    icon: androidx.compose.material.icons.Icons.Filled
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MergeHistorySection(mergeHistory: List<MergeHistoryItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Merge History",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (mergeHistory.isEmpty()) {
            Text(
                text = "No merge history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // XENOCOGNITIVE: Genealogy view shows prompt merge history
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    mergeHistory.forEachIndexed { index, item ->
                        MergeTimelineItem(
                            item = item,
                            isLast = index == mergeHistory.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MergeTimelineItem(
    item: MergeHistoryItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Merge rule",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }

        // Event details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.ruleName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = formatInstant(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Confidence: ${(item.confidenceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                onClick = onCopy,
                label = "Copy",
                icon = Icons.Default.FileCopy,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                onClick = onShare,
                label = "Share",
                icon = Icons.Default.Share,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                onClick = onExport,
                label = "Export",
                icon = Icons.Default.GetApp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.material.icons.Icons.Filled,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RelatedPromptsSection(relatedPrompts: List<RelatedPrompt>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Related Prompts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (relatedPrompts.isEmpty()) {
            Text(
                text = "No related prompts found",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            relatedPrompts.forEach { prompt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prompt.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Similarity: ${(prompt.similarity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View related prompt",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Data models for detail screen
data class DetailPromptModel(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val usageCount: Int,
    val complexity: String,
    val lastUsed: Instant?,
    val mergeHistory: List<MergeHistoryItem>,
    val relatedPrompts: List<RelatedPrompt>
)

data class MergeHistoryItem(
    val sessionId: String,
    val ruleName: String,
    val timestamp: Instant,
    val confidenceScore: Float
)

data class RelatedPrompt(
    val id: Long,
    val title: String,
    val similarity: Float
)

// Mock data generator
private fun generateMockPromptDetail(id: Long): DetailPromptModel {
    return DetailPromptModel(
        id = id,
        title = "System Prompt v${id}",
        content = """
            You are a helpful AI assistant. Your role is to provide accurate,
            thoughtful responses to user queries. Consider the context carefully
            before responding. Always prioritize accuracy and honesty.

            Key guidelines:
            - Be concise but thorough
            - Ask clarifying questions if needed
            - Acknowledge limitations
            - Provide citations when appropriate
        """.trimIndent(),
        createdAt = Instant.now().minusSeconds(3600),
        updatedAt = Instant.now().minusSeconds(600),
        usageCount = 23,
        complexity = "INTERMEDIATE",
        lastUsed = Instant.now().minusSeconds(300),
        mergeHistory = listOf(
            MergeHistoryItem(
                sessionId = "session-001",
                ruleName = "Merge: Safety Guidelines + System Prompt",
                timestamp = Instant.now().minusSeconds(7200),
                confidenceScore = 0.87f
            ),
            MergeHistoryItem(
                sessionId = "session-002",
                ruleName = "Refinement: Added context boundaries",
                timestamp = Instant.now().minusSeconds(3600),
                confidenceScore = 0.92f
            )
        ),
        relatedPrompts = listOf(
            RelatedPrompt(
                id = id + 1,
                title = "Assistant Behavior Guidelines",
                similarity = 0.85f
            ),
            RelatedPrompt(
                id = id + 2,
                title = "Safety Constraints",
                similarity = 0.78f
            ),
            RelatedPrompt(
                id = id + 3,
                title = "Content Policy",
                similarity = 0.72f
            )
        )
    )
}

private fun formatInstant(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
