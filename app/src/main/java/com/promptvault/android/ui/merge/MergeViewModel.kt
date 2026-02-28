package com.promptvault.android.ui.merge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptvault.android.data.model.Prompt
import com.promptvault.android.data.model.MergeRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * MergeViewModel: State management for merge workflow
 *
 * Responsibilities:
 * - Load and manage merge rules
 * - Handle rule selection and template preview
 * - Preview merge operations before execution
 * - Execute merge and save as new prompt
 * - Maintain undo/redo history
 *
 * References:
 * - TIMELINE.md Milestone 2.3 (Auto-Merge Engine) & Milestone 2.4 (Merge UI)
 * - ARCHITECTURE.md Section 5 (State Management)
 * - XENOCOGNITIVE_IDEAS.md #3 (Confidence Synesthesia)
 * - PRESSURE_POINTS.md PP-U3 (Confidence Anxiety Mitigation)
 */

// UI State for Merge Screen
sealed class MergeState {
    object Idle : MergeState()
    object Loading : MergeState()
    object Error : MergeState()
    data class Success(val data: MergeData) : MergeState()
    data class Preview(val data: MergePreviewData) : MergeState()
}

data class MergeData(
    val rules: List<MergeRule> = emptyList(),
    val selectedRule: MergeRule? = null,
    val inputPrompts: List<Prompt> = emptyList(),
    val selectedInputs: List<Prompt> = emptyList(),
    val mergeHistory: List<MergeHistoryItem> = emptyList()
)

data class MergePreviewData(
    val rule: MergeRule,
    val inputPrompts: List<Prompt>,
    val previewOutput: String = "",
    val confidenceScore: Float = 0.5f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

data class MergeHistoryItem(
    val sessionId: String,
    val ruleName: String,
    val inputCount: Int,
    val outputPromptId: Long? = null,
    val timestamp: Instant,
    val confidenceScore: Float
)

@HiltViewModel
class MergeViewModel @Inject constructor(
    // TODO: Inject repositories
    // private val promptRepository: PromptRepository,
    // private val mergeRuleRepository: MergeRuleRepository,
    // private val mergerEngine: AutoMergerEngine
) : ViewModel() {

    private val _mergeState = MutableStateFlow<MergeState>(MergeState.Idle)
    val mergeState: StateFlow<MergeState> = _mergeState.asStateFlow()

    private val _mergeData = MutableStateFlow(MergeData())
    val mergeData: StateFlow<MergeData> = _mergeData.asStateFlow()

    private val _mergePreview = MutableStateFlow<MergePreviewData?>(null)
    val mergePreview: StateFlow<MergePreviewData?> = _mergePreview.asStateFlow()

    private val _undoStack = MutableStateFlow<List<MergeHistoryItem>>(emptyList())
    val undoStack: StateFlow<List<MergeHistoryItem>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<MergeHistoryItem>>(emptyList())
    val redoStack: StateFlow<List<MergeHistoryItem>> = _redoStack.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Load all available merge rules from repository
     */
    fun loadRules() {
        viewModelScope.launch {
            _mergeState.value = MergeState.Loading
            try {
                // TODO: Fetch from mergeRuleRepository
                val rules = emptyList<MergeRule>() // mergeRuleRepository.getAllRules()

                _mergeData.value = _mergeData.value.copy(rules = rules)
                _mergeState.value = MergeState.Success(_mergeData.value)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load merge rules: ${e.message}"
                _mergeState.value = MergeState.Error
            }
        }
    }

    /**
     * Load all available prompts for selection as merge inputs
     */
    fun loadInputPrompts() {
        viewModelScope.launch {
            try {
                // TODO: Fetch from promptRepository
                val prompts = emptyList<Prompt>() // promptRepository.getAllPrompts()

                _mergeData.value = _mergeData.value.copy(inputPrompts = prompts)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load prompts: ${e.message}"
            }
        }
    }

    /**
     * Select a merge rule for preview
     */
    fun selectRule(rule: MergeRule) {
        _mergeData.value = _mergeData.value.copy(selectedRule = rule)
        _errorMessage.value = null

        // Auto-preview if we have selected inputs
        if (_mergeData.value.selectedInputs.isNotEmpty()) {
            previewMerge()
        }
    }

    /**
     * Toggle prompt selection for merge inputs (multi-select)
     */
    fun togglePromptSelection(prompt: Prompt) {
        val currentSelection = _mergeData.value.selectedInputs
        val newSelection = if (prompt in currentSelection) {
            currentSelection - prompt
        } else {
            currentSelection + prompt
        }

        _mergeData.value = _mergeData.value.copy(selectedInputs = newSelection)

        // Update preview if rule is selected
        if (_mergeData.value.selectedRule != null && newSelection.isNotEmpty()) {
            previewMerge()
        }
    }

    /**
     * Clear all selected input prompts
     */
    fun clearSelection() {
        _mergeData.value = _mergeData.value.copy(selectedInputs = emptyList())
        _mergePreview.value = null
    }

    /**
     * Preview merge result without executing
     * Calculates confidence score based on input quality
     */
    fun previewMerge() {
        val rule = _mergeData.value.selectedRule
        val inputs = _mergeData.value.selectedInputs

        if (rule == null || inputs.isEmpty()) {
            _mergePreview.value = null
            return
        }

        viewModelScope.launch {
            try {
                _mergeState.value = MergeState.Loading

                // TODO: Call mergerEngine.previewMerge(rule, inputs)
                // Returns: (previewOutput, confidenceScore)
                val previewOutput = """
                    |[MERGE PREVIEW]
                    |Rule: ${rule.name}
                    |Template: ${rule.template}
                    |
                    |Inputs:
                    |${inputs.joinToString("\n---\n") { "• ${it.title}" }}
                    |
                    |[Confidence calculation in progress...]
                """.trimMargin()

                val confidenceScore = calculateConfidenceScore(rule, inputs)

                _mergePreview.value = MergePreviewData(
                    rule = rule,
                    inputPrompts = inputs,
                    previewOutput = previewOutput,
                    confidenceScore = confidenceScore,
                    canUndo = _undoStack.value.isNotEmpty(),
                    canRedo = _redoStack.value.isNotEmpty()
                )

                _mergeState.value = MergeState.Preview(_mergePreview.value!!)
            } catch (e: Exception) {
                _errorMessage.value = "Preview failed: ${e.message}"
                _mergeState.value = MergeState.Error
            }
        }
    }

    /**
     * Execute the merge operation and save as new prompt
     * This is the main action button in MergeScreen
     * Requires: rule selected + at least 1 input prompt selected
     */
    fun executeMerge(newPromptTitle: String = "") {
        val rule = _mergeData.value.selectedRule
        val inputs = _mergeData.value.selectedInputs
        val preview = _mergePreview.value

        if (rule == null || inputs.isEmpty() || preview == null) {
            _errorMessage.value = "Cannot execute merge: missing rule or inputs"
            return
        }

        viewModelScope.launch {
            try {
                _mergeState.value = MergeState.Loading

                // TODO: Call mergerEngine.execute(rule, inputs)
                val mergedContent = preview.previewOutput
                val title = if (newPromptTitle.isNotEmpty()) {
                    newPromptTitle
                } else {
                    "Merged: ${rule.name} (${Instant.now().epochSecond % 10000})"
                }

                // Create new prompt from merge output
                val mergedPrompt = Prompt(
                    title = title,
                    content = mergedContent,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now(),
                    mergeHistory = listOf(
                        com.promptvault.android.data.model.MergeRecord(
                            sessionId = "merge-${Instant.now().epochSecond}",
                            ruleName = rule.name,
                            timestamp = Instant.now(),
                            confidenceScore = preview.confidenceScore
                        )
                    )
                )

                // TODO: Save to promptRepository
                // val savedId = promptRepository.insert(mergedPrompt)

                // Record in merge history
                val historyItem = MergeHistoryItem(
                    sessionId = "merge-${Instant.now().epochSecond}",
                    ruleName = rule.name,
                    inputCount = inputs.size,
                    outputPromptId = 1L, // TODO: Use actual saved ID
                    timestamp = Instant.now(),
                    confidenceScore = preview.confidenceScore
                )

                // Push to undo stack
                _undoStack.value = _undoStack.value + historyItem
                _redoStack.value = emptyList() // Clear redo on new action

                _mergeData.value = _mergeData.value.copy(
                    mergeHistory = _mergeData.value.mergeHistory + historyItem
                )

                _mergeState.value = MergeState.Success(_mergeData.value)
                _errorMessage.value = null

                // Clear selection for next merge
                clearSelection()
            } catch (e: Exception) {
                _errorMessage.value = "Merge execution failed: ${e.message}"
                _mergeState.value = MergeState.Error
            }
        }
    }

    /**
     * Undo last merge operation
     * Implements undo/redo stack pattern
     */
    fun undo() {
        if (_undoStack.value.isEmpty()) {
            _errorMessage.value = "Nothing to undo"
            return
        }

        viewModelScope.launch {
            val lastMerge = _undoStack.value.last()
            _undoStack.value = _undoStack.value.dropLast(1)
            _redoStack.value = _redoStack.value + lastMerge

            // TODO: Delete the merged prompt from repository
            // promptRepository.delete(lastMerge.outputPromptId)

            _mergeData.value = _mergeData.value.copy(
                mergeHistory = _mergeData.value.mergeHistory.dropLast(1)
            )

            _mergeState.value = MergeState.Success(_mergeData.value)
            _errorMessage.value = null
        }
    }

    /**
     * Redo last undone merge operation
     */
    fun redo() {
        if (_redoStack.value.isEmpty()) {
            _errorMessage.value = "Nothing to redo"
            return
        }

        viewModelScope.launch {
            val lastUndone = _redoStack.value.last()
            _redoStack.value = _redoStack.value.dropLast(1)
            _undoStack.value = _undoStack.value + lastUndone

            // TODO: Re-create the merged prompt in repository

            _mergeData.value = _mergeData.value.copy(
                mergeHistory = _mergeData.value.mergeHistory + lastUndone
            )

            _mergeState.value = MergeState.Success(_mergeData.value)
            _errorMessage.value = null
        }
    }

    /**
     * Save merge output as new prompt
     * This is a wrapper around executeMerge with explicit title/content params
     */
    fun saveAsPrompt(promptTitle: String, promptContent: String) {
        if (promptTitle.isBlank() || promptContent.isBlank()) {
            _errorMessage.value = "Title and content cannot be empty"
            return
        }
        executeMerge(promptTitle)
    }

    /**
     * Dismiss any error messages
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Calculate confidence score for merge
     * Based on:
     * - Rule complexity
     * - Input prompt length and clarity
     * - Variable substitution validity
     * - Historical success of this rule
     *
     * Returns: 0.0 (low confidence) to 1.0 (high confidence)
     */
    private suspend fun calculateConfidenceScore(rule: MergeRule, inputs: List<Prompt>): Float {
        try {
            // TODO: Implement confidence scoring algorithm
            // For now, return a mock score based on input count
            return when {
                inputs.size < 2 -> 0.3f
                inputs.size == 2 -> 0.6f
                else -> 0.7f
            }
        } catch (e: Exception) {
            return 0.5f // Neutral confidence if calculation fails
        }
    }
}
