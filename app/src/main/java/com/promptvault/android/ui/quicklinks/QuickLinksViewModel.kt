package com.promptvault.android.ui.quicklinks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptvault.android.data.model.QuickLink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * QuickLinksViewModel: Quick link management and deep linking
 *
 * Responsibilities:
 * - Load and manage user's quick links
 * - Create, update, delete quick links
 * - Generate and manage deep link URIs
 * - Handle version management for deep link schema
 * - Support reordering quick links
 *
 * References:
 * - TIMELINE.md Milestone 2.5 (Quick Links & Deep Linking)
 * - DEVOPS.md DeepLinkHandler v2 (Versioned deep links)
 * - ARCHITECTURE.md Section 5 (State Management)
 *
 * Comments:
 * - Versioned deep links per DEVOPS.md DeepLinkHandler v2
 */

@HiltViewModel
class QuickLinksViewModel @Inject constructor(
    // TODO: Inject repositories
    // private val quickLinkRepository: QuickLinkRepository,
    // private val deepLinkHandler: DeepLinkHandler
) : ViewModel() {

    private val _quickLinks = MutableStateFlow<List<QuickLink>>(emptyList())
    val quickLinks: StateFlow<List<QuickLink>> = _quickLinks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Deep link URI version (for schema migration)
    private val CURRENT_DEEP_LINK_VERSION = 2

    /**
     * Load all quick links from repository
     */
    fun loadQuickLinks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Load from quickLinkRepository
                val links = emptyList<QuickLink>() // quickLinkRepository.getAllQuickLinks()

                // Sort by sortOrder
                _quickLinks.value = links.sortedBy { it.sortOrder }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load quick links: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new quick link with generated deep link URI
     */
    fun createQuickLink(name: String, description: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Generate deep link URI based on selected prompt/collection
                // TODO: Show selection dialog to pick target prompt/collection
                val targetId = 1L // Placeholder
                val deepLinkUri = generateDeepLink(targetId)

                val newQuickLink = QuickLink(
                    name = name,
                    description = description,
                    deepLinkUri = deepLinkUri,
                    uriVersion = CURRENT_DEEP_LINK_VERSION,
                    targetPromptId = targetId,
                    sortOrder = (_quickLinks.value.maxOfOrNull { it.sortOrder } ?: 0) + 1,
                    createdAt = Instant.now()
                )

                // TODO: Insert to quickLinkRepository
                // quickLinkRepository.insert(newQuickLink)

                // Update local state
                _quickLinks.value = (_quickLinks.value + newQuickLink).sortedBy { it.sortOrder }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create quick link: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing quick link
     */
    fun updateQuickLink(id: Long, name: String, description: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val updated = _quickLinks.value
                    .find { it.id == id }
                    ?.copy(
                        name = name,
                        description = description
                    ) ?: return@launch

                // TODO: Update in quickLinkRepository
                // quickLinkRepository.update(updated)

                // Update local state
                _quickLinks.value = _quickLinks.value
                    .map { if (it.id == id) updated else it }
                    .sortedBy { it.sortOrder }

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update quick link: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a quick link
     */
    fun deleteQuickLink(id: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // TODO: Delete from quickLinkRepository
                // quickLinkRepository.delete(id)

                // Update local state
                _quickLinks.value = _quickLinks.value.filter { it.id != id }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete quick link: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reorder quick links (drag-to-reorder)
     * direction: -1 for move up, +1 for move down
     */
    fun reorderQuickLink(id: Long, direction: Int) {
        viewModelScope.launch {
            try {
                val currentList = _quickLinks.value.sortedBy { it.sortOrder }
                val index = currentList.indexOfFirst { it.id == id }

                if (index < 0) return@launch

                val newIndex = index + direction
                if (newIndex < 0 || newIndex >= currentList.size) return@launch

                // Swap sort orders
                val item1 = currentList[index]
                val item2 = currentList[newIndex]

                val updated1 = item1.copy(sortOrder = item2.sortOrder)
                val updated2 = item2.copy(sortOrder = item1.sortOrder)

                // TODO: Update both in quickLinkRepository
                // quickLinkRepository.update(updated1)
                // quickLinkRepository.update(updated2)

                // Update local state
                _quickLinks.value = _quickLinks.value
                    .map {
                        when (it.id) {
                            id -> updated1
                            item2.id -> updated2
                            else -> it
                        }
                    }
                    .sortedBy { it.sortOrder }

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reorder quick link: ${e.message}"
            }
        }
    }

    /**
     * Generate versioned deep link URI
     * Format: promptvault://v{version}/prompt/{id}
     *
     * References:
     * - TIMELINE.md Milestone 2.5: Deep link migration logic
     * - RISK_ASSESSMENT.md MR-001: Deep link versioning prevents breaks
     */
    private fun generateDeepLink(targetId: Long): String {
        return "promptvault://v$CURRENT_DEEP_LINK_VERSION/prompt/$targetId"
    }

    /**
     * Handle legacy deep link migration
     * Called by DeepLinkHandler when receiving old-format URIs
     */
    fun migrateDeepLink(oldUri: String): String {
        // Example: Upgrade "promptvault://prompt/123" to "promptvault://v2/prompt/123"
        return when {
            oldUri.startsWith("promptvault://prompt/") -> {
                val id = oldUri.removePrefix("promptvault://prompt/")
                "promptvault://v$CURRENT_DEEP_LINK_VERSION/prompt/$id"
            }
            oldUri.startsWith("promptvault://v") -> oldUri // Already versioned
            else -> oldUri // Unknown format
        }
    }
}
