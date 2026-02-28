package com.promptvault.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.promptvault.android.data.model.Prompt
import com.promptvault.android.data.model.MergeRule
import com.promptvault.android.ui.components.ConfidenceIndicator
import com.promptvault.android.ui.merge.MergePreviewData
import com.promptvault.android.ui.merge.MergeState
import com.promptvault.android.ui.merge.MergeViewModel

/**
 * MergeScreen: Advanced merge workflow
 *
 * Responsibilities:
 * 1. Rule selector (dropdown showing merge rules with preview)
 * 2. Template preview (read-only display of merge template)
 * 3. Multi-select input prompt picker
 * 4. Confidence score gauge (color gradient red→yellow→green)
 * 5. Merge preview (side-by-side before/after comparison)
 * 6. "Execute Merge" button → "Save as New Prompt" flow
 * 7. Merge history display with undo/redo
 *
 * References:
 * - TIMELINE.md Milestone 2.4 (Merge UI Screen)
 * - XENOCOGNITIVE_IDEAS.md #3 (Synesthesia Confidence)
 * - PRESSURE_POINTS.md PP-U3 (Confidence Anxiety Mitigation)
 *
 * Comments:
 * - PP-U3: Confidence anxiety mitigation - Add social proof + undo option
 * - XENOCOGNITIVE: Synesthesia - Color + haptic + audio feedback
 */

@Composable
fun MergeScreen(
    modifier: Modifier = Modifier,
    viewModel: MergeViewModel = hiltViewModel()
) {
    val mergeState by viewModel.mergeState.collectAsStateWithLifecycle()
    val mergeData by viewModel.mergeData.collectAsStateWithLifecycle()
    val mergePreview by viewModel.mergePreview.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val undoStack by viewModel.undoStack.collectAsStateWithLifecycle()
    val redoStack by viewModel.redoStack.collectAsStateWithLifecycle()

    var showRuleDropdown by remember { mutableStateOf(false) }
    var showNewPromptDialog by remember { mutableStateOf(false) }
    var newPromptTitle by remember { mutableStateOf("") }

    // Load rules and prompts on first composition
    LaunchedEffect(Unit) {
        viewModel.loadRules()
        viewModel.loadInputPrompts()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Merge Prompts",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Error message display
            if (!errorMessage.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 1. RULE SELECTOR DROPDOWN
            item {
                RuleSelectorDropdown(
                    selectedRule = mergeData.selectedRule,
                    allRules = mergeData.rules,
                    showDropdown = showRuleDropdown,
                    onShowDropdownChange = { showRuleDropdown = it },
                    onRuleSelected = { rule ->
                        viewModel.selectRule(rule)
                        showRuleDropdown = false
                    }
                )
            }

            // 2. TEMPLATE PREVIEW (read-only)
            if (mergeData.selectedRule != null) {
                item {
                    TemplatePreview(rule = mergeData.selectedRule!!)
                }
            }

            // 3. MULTI-SELECT INPUT PROMPT PICKER
            item {
                InputPromptPicker(
                    availablePrompts = mergeData.inputPrompts,
                    selectedPrompts = mergeData.selectedInputs,
                    onPromptToggled = { prompt ->
                        viewModel.togglePromptSelection(prompt)
                    },
                    onClearSelection = { viewModel.clearSelection() }
                )
            }

            // 4. CONFIDENCE SCORE INDICATOR
            if (mergePreview != null) {
                item {
                    ConfidenceIndicator(
                        score = mergePreview.confidenceScore,
                        modifier = Modifier.padding(vertical = 16.dp),
                        enableHaptics = false, // TODO: Enable when haptic integration ready
                        enableAudio = false,   // TODO: Enable when audio integration ready
                        showLabel = true
                    )
                }
            }

            // 5. MERGE PREVIEW (side-by-side before/after)
            if (mergePreview != null) {
                item {
                    MergePreviewSection(preview = mergePreview)
                }
            }

            // 6. ACTION BUTTONS: Execute Merge / Save as Prompt
            if (mergeData.selectedRule != null && mergeData.selectedInputs.isNotEmpty()) {
                item {
                    ExecuteMergeActions(
                        isLoading = mergeState is MergeState.Loading,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                        onExecuteMerge = {
                            showNewPromptDialog = true
                        },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() }
                    )
                }
            }

            // 7. MERGE HISTORY DISPLAY
            if (mergeData.mergeHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Merges",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                    )
                }

                items(mergeData.mergeHistory.takeLast(5)) { history ->
                    MergeHistoryCard(
                        history = history,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Spacer at end
            item {
                Box(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog: Save merged prompt as new prompt
    if (showNewPromptDialog) {
        SaveMergedPromptDialog(
            onConfirm = { title ->
                viewModel.saveAsPrompt(title, mergePreview?.previewOutput.orEmpty())
                showNewPromptDialog = false
                newPromptTitle = ""
            },
            onDismiss = { showNewPromptDialog = false }
        )
    }
}

@Composable
private fun RuleSelectorDropdown(
    selectedRule: MergeRule?,
    allRules: List<MergeRule>,
    showDropdown: Boolean,
    onShowDropdownChange: (Boolean) -> Unit,
    onRuleSelected: (MergeRule) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "1. Select Merge Rule",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Dropdown button
        OutlinedButton(
            onClick = { onShowDropdownChange(!showDropdown) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = selectedRule?.name ?: "Choose a merge rule...",
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
        }

        // Dropdown menu
        if (showDropdown && allRules.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column {
                    allRules.forEach { rule ->
                        RuleDropdownItem(
                            rule = rule,
                            isSelected = selectedRule?.ruleId == rule.ruleId,
                            onClick = { onRuleSelected(rule) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleDropdownItem(
    rule: MergeRule,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rule.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (rule.description.isNotEmpty()) {
                Text(
                    text = rule.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TemplatePreview(
    rule: MergeRule,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = "2. Template Preview",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Template (Read-Only)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rule.template,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InputPromptPicker(
    availablePrompts: List<Prompt>,
    selectedPrompts: List<Prompt>,
    onPromptToggled: (Prompt) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3. Select Input Prompts (${selectedPrompts.size} selected)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (selectedPrompts.isNotEmpty()) {
                OutlinedButton(onClick = onClearSelection) {
                    Text("Clear")
                }
            }
        }

        if (availablePrompts.isEmpty()) {
            Text(
                text = "No prompts available. Create some prompts first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    availablePrompts.forEach { prompt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = prompt in selectedPrompts,
                                onCheckedChange = { onPromptToggled(prompt) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prompt.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = prompt.content.take(50) + if (prompt.content.length > 50) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MergePreviewSection(
    preview: MergePreviewData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = "5. Merge Preview",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Output Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = preview.previewOutput,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ExecuteMergeActions(
    isLoading: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onExecuteMerge: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        // Main action button
        Button(
            onClick = onExecuteMerge,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(20.dp)
                        .padding(8.dp),
                    strokeWidth = 2.dp
                )
            }
            Text("6. Execute Merge")
        }

        // Undo/Redo buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier.weight(1f),
                enabled = canUndo
            ) {
                Text("↶ Undo")
            }
            OutlinedButton(
                onClick = onRedo,
                modifier = Modifier.weight(1f),
                enabled = canRedo
            ) {
                Text("↷ Redo")
            }
        }

        // PP-U3: Social proof + undo option for confidence anxiety mitigation
        Text(
            text = "✓ You can always undo this merge",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MergeHistoryCard(
    history: com.promptvault.android.ui.merge.MergeHistoryItem,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = history.ruleName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${history.inputCount} inputs → Output #${history.outputPromptId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = history.timestamp.toString().take(19),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SaveMergedPromptDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptTitle by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as New Prompt") },
        text = {
            OutlinedTextField(
                value = promptTitle,
                onValueChange = { promptTitle = it },
                label = { Text("Prompt Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(promptTitle) },
                enabled = promptTitle.isNotBlank()
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
