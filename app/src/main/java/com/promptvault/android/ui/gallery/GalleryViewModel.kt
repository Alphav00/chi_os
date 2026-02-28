package com.promptvault.android.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

// UI State classes
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val userMessage: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

data class PromptUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val preview: String,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val lastUsed: Instant? = null,
    val tags: List<String> = emptyList(),
    val complexity: String = "INTERMEDIATE"
)

data class GalleryUiState(
    val prompts: List<PromptUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val selectedComplexity: String? = null,
    val dateRangeStart: Instant? = null,
    val dateRangeEnd: Instant? = null,
    val sortBy: SortOrder = SortOrder.RECENT,
    val undoAction: UndoAction? = null
)

enum class SortOrder {
    RECENT, OLDEST, MOST_USED, TITLE_ASC, TITLE_DESC
}

data class UndoAction(
    val promptId: Long,
    val promptData: PromptUiModel,
    val action: String // "delete", etc.
)

class GalleryViewModel : ViewModel() {
    // State management
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    // MVVM: State flows for individual properties
    private val _prompts = MutableStateFlow<List<PromptUiModel>>(emptyList())
    val prompts: StateFlow<List<PromptUiModel>> = _prompts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPrompts()
    }

    // PP-T1: Pagination for scale - LazyVerticalGrid loads 100 prompts efficiently
    fun loadPrompts() {
        viewModelScope.launch {
            try {
                _isLoading.emit(true)
                // Simulate loading from repository (Room database with Paging 3)
                val mockPrompts = generateMockPrompts(100)
                _prompts.emit(mockPrompts)
                _uiState.update {
                    it.copy(
                        prompts = mockPrompts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _isLoading.emit(false)
                _error.emit(e.message ?: "Unknown error")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    // Search functionality with real-time filtering
    fun searchPrompts(query: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(searchQuery = query, isLoading = true) }

                if (query.isEmpty()) {
                    loadPrompts()
                } else {
                    // Filter prompts by search query
                    val filtered = _prompts.value.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
                    }
                    _uiState.update {
                        it.copy(
                            prompts = filtered,
                            searchQuery = query,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Search failed"
                    )
                }
            }
        }
    }

    // Filter by tags
    fun filterByTag(tag: String) {
        viewModelScope.launch {
            val currentTags = _uiState.value.selectedTags.toMutableSet()
            if (currentTags.contains(tag)) {
                currentTags.remove(tag)
            } else {
                currentTags.add(tag)
            }

            applyFilters(
                tags = currentTags,
                complexity = _uiState.value.selectedComplexity
            )
        }
    }

    // Filter by complexity
    fun filterByComplexity(complexity: String) {
        viewModelScope.launch {
            applyFilters(
                tags = _uiState.value.selectedTags,
                complexity = if (_uiState.value.selectedComplexity == complexity) null else complexity
            )
        }
    }

    // Filter by date range
    fun filterByDateRange(start: Instant, end: Instant) {
        viewModelScope.launch {
            applyFilters(
                tags = _uiState.value.selectedTags,
                complexity = _uiState.value.selectedComplexity,
                dateStart = start,
                dateEnd = end
            )
        }
    }

    // Internal: Apply all active filters
    private suspend fun applyFilters(
        tags: Set<String> = _uiState.value.selectedTags,
        complexity: String? = _uiState.value.selectedComplexity,
        dateStart: Instant? = _uiState.value.dateRangeStart,
        dateEnd: Instant? = _uiState.value.dateRangeEnd
    ) {
        try {
            var filtered = _prompts.value

            // Filter by tags
            if (tags.isNotEmpty()) {
                filtered = filtered.filter { prompt ->
                    prompt.tags.any { it in tags }
                }
            }

            // Filter by complexity
            if (complexity != null) {
                filtered = filtered.filter { it.complexity == complexity }
            }

            // Filter by date range
            if (dateStart != null && dateEnd != null) {
                filtered = filtered.filter {
                    it.createdAt >= dateStart && it.createdAt <= dateEnd
                }
            }

            _uiState.update {
                it.copy(
                    prompts = filtered,
                    selectedTags = tags,
                    selectedComplexity = complexity,
                    dateRangeStart = dateStart,
                    dateRangeEnd = dateEnd
                )
            }
        } catch (e: Exception) {
            _error.emit(e.message ?: "Filter failed")
        }
    }

    // Toggle favorite status
    fun toggleFavorite(promptId: Long) {
        viewModelScope.launch {
            try {
                val updated = _prompts.value.map { prompt ->
                    if (prompt.id == promptId) {
                        prompt.copy(isFavorite = !prompt.isFavorite)
                    } else {
                        prompt
                    }
                }
                _prompts.emit(updated)

                _uiState.update { state ->
                    state.copy(
                        prompts = state.prompts.map { prompt ->
                            if (prompt.id == promptId) {
                                prompt.copy(isFavorite = !prompt.isFavorite)
                            } else {
                                prompt
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _error.emit("Failed to update favorite: ${e.message}")
            }
        }
    }

    // Swipe-to-delete with undo gesture
    fun deletePrompt(promptId: Long) {
        viewModelScope.launch {
            try {
                val promptToDelete = _prompts.value.find { it.id == promptId }
                if (promptToDelete != null) {
                    // Remove from list
                    val updated = _prompts.value.filter { it.id != promptId }
                    _prompts.emit(updated)

                    // Store undo action
                    val undoAction = UndoAction(
                        promptId = promptId,
                        promptData = promptToDelete,
                        action = "delete"
                    )

                    _uiState.update {
                        it.copy(
                            prompts = it.prompts.filter { p -> p.id != promptId },
                            undoAction = undoAction
                        )
                    }
                }
            } catch (e: Exception) {
                _error.emit("Failed to delete prompt: ${e.message}")
            }
        }
    }

    // Undo last action
    fun undoLastAction() {
        viewModelScope.launch {
            try {
                val undoAction = _uiState.value.undoAction ?: return@launch

                when (undoAction.action) {
                    "delete" -> {
                        val restored = _prompts.value.toMutableList()
                        restored.add(undoAction.promptData)
                        _prompts.emit(restored)

                        _uiState.update {
                            it.copy(
                                prompts = it.prompts + undoAction.promptData,
                                undoAction = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _error.emit("Failed to undo: ${e.message}")
            }
        }
    }

    // Clear error message
    fun clearError() {
        viewModelScope.launch {
            _error.emit(null)
            _uiState.update { it.copy(error = null) }
        }
    }

    // Sort prompts
    fun sortBy(sortOrder: SortOrder) {
        viewModelScope.launch {
            val sorted = when (sortOrder) {
                SortOrder.RECENT -> _prompts.value.sortedByDescending { it.createdAt }
                SortOrder.OLDEST -> _prompts.value.sortedBy { it.createdAt }
                SortOrder.MOST_USED -> _prompts.value.sortedByDescending { it.usageCount }
                SortOrder.TITLE_ASC -> _prompts.value.sortedBy { it.title }
                SortOrder.TITLE_DESC -> _prompts.value.sortedByDescending { it.title }
            }

            _uiState.update {
                it.copy(
                    prompts = sorted,
                    sortBy = sortOrder
                )
            }
        }
    }

    // Mock data generator for testing
    private fun generateMockPrompts(count: Int): List<PromptUiModel> {
        return (1..count).map { i ->
            PromptUiModel(
                id = i.toLong(),
                title = "Prompt $i",
                content = "This is the full content of prompt $i. It contains detailed information about the topic.",
                preview = "This is the full content of prompt $i. It contains detailed information...".take(100),
                isFavorite = i % 5 == 0,
                usageCount = (i % 20),
                createdAt = Instant.now().minusSeconds((i * 3600).toLong()),
                lastUsed = if (i % 3 == 0) Instant.now().minusSeconds((i * 1800).toLong()) else null,
                tags = listOf("tag${i % 5}", "category${i % 3}"),
                complexity = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT")[i % 4]
            )
        }
    }
}
