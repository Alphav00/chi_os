package com.promptvault.android.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.promptvault.android.data.dao.PromptDao
import com.promptvault.android.data.model.Prompt
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

/**
 * PromptRepository: Data access abstraction for Prompt operations.
 *
 * Responsibility: Mediate between DAO and ViewModel, implement caching, error handling.
 * Owner: Backend/Repository Engineer (ARCHITECTURE.md, Section 3.1)
 *
 * Risk References:
 * - CR-001 (Data Loss): Implements backup-critical operations with validation
 * - PP-T1 (Performance): Paging 3 for efficient large-dataset handling
 * - HR-004 (Perf Degradation): Query optimization + pagination for scale
 *
 * Design: Repository pattern with suspend functions for async operations.
 * All database operations are non-blocking via coroutines.
 *
 * Deliverable: Functional repository connected to database (Milestone 2.1)
 */
class PromptRepository @Inject constructor(
    private val promptDao: PromptDao
) {

    companion object {
        // Pagination configuration (from ARCHITECTURE.md, Section 6.2)
        private const val PAGE_SIZE = 50
        private const val PREFETCH_DISTANCE = 10
    }

    // ===== Paging 3 Integration (Milestone 2.1) =====
    // Infinite scroll with lazy loading

    /**
     * Returns a PagingData flow of all prompts sorted by creation date (newest first).
     * Used for infinite scroll gallery display.
     *
     * Performance: Gallery load <500ms (ARCHITECTURE.md Section 7)
     *
     * @return Flow of PagingData for LazyVerticalGrid consumption
     */
    fun getAllPromptsPagedFlow(): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { promptDao.getAllPromptsPagingSource() }
        ).flow
    }

    /**
     * Returns a PagingData flow of search results.
     * Used for search functionality with FTS.
     *
     * Performance: Search <300ms (ARCHITECTURE.md Section 7)
     * Risk: HR-004 mitigation - FTS index for fast search
     *
     * @param query Search term
     * @return Flow of PagingData for search results
     */
    fun searchPromptsFlow(query: String): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { promptDao.searchPromptsPagingSource(query) }
        ).flow
    }

    // ===== Filter Operations (Milestone 2.1) =====

    /**
     * Returns favorite prompts with pagination.
     *
     * @return Flow of PagingData of favorite prompts
     */
    fun getFavoritePromptsFlow(): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = { promptDao.getFavoritePagingSource() }
        ).flow
    }

    /**
     * Returns prompts filtered by complexity level.
     *
     * @param complexity Complexity level filter
     * @return Flow of PagingData of filtered prompts
     */
    fun getPromptsByComplexityFlow(complexity: String): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = { promptDao.getPromptsByComplexityPagingSource(complexity) }
        ).flow
    }

    /**
     * Returns prompts by date range filter.
     *
     * @param startDate Start date (milliseconds since epoch)
     * @param endDate End date (milliseconds since epoch)
     * @return Flow of PagingData of filtered prompts
     */
    fun getPromptsByDateRangeFlow(startDate: Long, endDate: Long): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = {
                promptDao.getPromptsByDateRangePagingSource(startDate, endDate)
            }
        ).flow
    }

    /**
     * Returns most-used prompts with pagination.
     *
     * @return Flow of PagingData of most-used prompts
     */
    fun getMostUsedPromptsFlow(): Flow<PagingData<Prompt>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = { promptDao.getMostUsedPagingSource() }
        ).flow
    }

    // ===== Single Prompt Operations =====

    /**
     * Gets a single prompt by ID (one-time read, non-reactive).
     *
     * @param promptId ID of the prompt
     * @return Prompt or null if not found
     */
    suspend fun getPromptById(promptId: Long): Prompt? {
        return promptDao.getPromptByIdOnce(promptId)
    }

    /**
     * Gets a single prompt by ID as a Flow for reactive updates.
     *
     * @param promptId ID of the prompt
     * @return Flow<Prompt?> that emits updates whenever prompt changes
     */
    fun getPromptByIdFlow(promptId: Long): Flow<Prompt?> {
        return promptDao.getPromptById(promptId)
    }

    // ===== CRUD Operations =====

    /**
     * Inserts a new prompt into the database.
     *
     * @param prompt Prompt to insert
     * @return ID of the newly inserted prompt
     */
    suspend fun insertPrompt(prompt: Prompt): Long {
        return promptDao.insertPrompt(prompt)
    }

    /**
     * Updates an existing prompt.
     *
     * @param prompt Prompt to update (identified by id)
     */
    suspend fun updatePrompt(prompt: Prompt) {
        promptDao.updatePrompt(prompt)
    }

    /**
     * Deletes a prompt by ID.
     * MR-003 Mitigation: Caller should implement undo window (5 seconds)
     *
     * @param promptId ID of prompt to delete
     */
    suspend fun deletePrompt(promptId: Long) {
        promptDao.deletePrompt(promptId)
    }

    /**
     * Deletes multiple prompts in a single transaction.
     * CR-001 Mitigation: Backup before bulk delete operations
     *
     * @param promptIds List of prompt IDs to delete
     */
    suspend fun deletePrompts(promptIds: List<Long>) {
        promptDao.deletePrompts(promptIds)
    }

    /**
     * Deletes all prompts (dangerous operation).
     * CR-001 Mitigation: Should only be called after user confirmation + backup
     */
    suspend fun deleteAllPrompts() {
        promptDao.deleteAllPrompts()
    }

    // ===== Usage Tracking =====

    /**
     * Increments usage count for a prompt.
     * Called whenever user interacts with (views/copies) a prompt.
     *
     * @param promptId ID of the prompt
     */
    suspend fun incrementUsageCount(promptId: Long) {
        promptDao.incrementUsageCount(promptId)
    }

    /**
     * Updates the last-used timestamp for a prompt.
     *
     * @param promptId ID of the prompt
     * @param timestamp Timestamp in milliseconds since epoch
     */
    suspend fun updateLastUsedTimestamp(promptId: Long, timestamp: Long) {
        promptDao.updateLastUsedTimestamp(promptId, timestamp)
    }

    /**
     * Records a prompt view/interaction.
     * Convenience method that updates both usage count and last-used timestamp.
     *
     * @param promptId ID of the prompt
     */
    suspend fun recordPromptInteraction(promptId: Long) {
        incrementUsageCount(promptId)
        updateLastUsedTimestamp(promptId, System.currentTimeMillis())
    }

    // ===== Favorite Operations =====

    /**
     * Sets favorite status for a prompt.
     *
     * @param promptId ID of the prompt
     * @param isFavorite True to mark as favorite, false to unmark
     */
    suspend fun setFavorite(promptId: Long, isFavorite: Boolean) {
        promptDao.setFavorite(promptId, isFavorite)
    }

    /**
     * Toggles favorite status for a prompt.
     *
     * @param promptId ID of the prompt
     */
    suspend fun toggleFavorite(promptId: Long) {
        val prompt = promptDao.getPromptByIdOnce(promptId) ?: return
        promptDao.setFavorite(promptId, !prompt.favorite)
    }

    // ===== Batch Operations =====

    /**
     * Inserts multiple prompts in a single transaction.
     * Used for bulk imports (JSON files, etc.).
     * CR-001 Mitigation: Used during restore operations with validation
     *
     * @param prompts List of prompts to insert
     */
    suspend fun insertPrompts(prompts: List<Prompt>) {
        promptDao.insertPrompts(prompts)
    }

    /**
     * Updates multiple prompts in a single transaction.
     *
     * @param prompts List of prompts to update
     */
    suspend fun updatePrompts(prompts: List<Prompt>) {
        promptDao.updatePrompts(prompts)
    }

    /**
     * Gets total count of prompts in database.
     * Used for pagination calculations.
     *
     * @return Flow emitting total prompt count
     */
    fun getPromptCount(): Flow<Int> {
        return promptDao.getPromptCount()
    }

    /**
     * Gets all prompts as a single Flow (non-paginated).
     * Only suitable for <100 prompts; use paging for larger datasets.
     *
     * @return Flow emitting list of all prompts
     */
    fun getAllPromptsFlow(): Flow<List<Prompt>> {
        return promptDao.getAllPromptsFlow()
    }

    /**
     * Gets unused prompts (not accessed within specified threshold).
     * Used for cleanup suggestions.
     *
     * @param thresholdEpochMilli Timestamp threshold in milliseconds
     * @return Flow emitting unused prompts
     */
    fun getUnusedPromptsFlow(thresholdEpochMilli: Long): Flow<List<Prompt>> {
        return promptDao.getUnusedPrompts(thresholdEpochMilli)
    }
}
