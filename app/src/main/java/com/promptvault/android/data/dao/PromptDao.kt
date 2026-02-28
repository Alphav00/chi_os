package com.promptvault.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.promptvault.android.data.model.Prompt
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Prompt entity.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Deliverables: DAO implementation, migration scripts
 * - Code Review: All data-layer PRs
 * - Expertise: Kotlin, Room/SQLite, MVVM architecture
 *
 * Risk References:
 * - PP-T1 (Performance): Pagination + FTS index for scale
 *   Optimization: Search latency <300ms (Milestone 3.5)
 *   Uses FTS (Full-Text Search) index on content for fast searches
 * - CR-001 (Data Loss): Backup-critical DAO
 *   Backup-critical: validate data integrity
 *
 * All DAOs return Flow<T> for reactive updates.
 * Pagination handled via limit/offset for large result sets.
 */
@Dao
interface PromptDao {

    // ===== CRUD Operations =====

    /**
     * Inserts a new prompt into the database.
     * Returns the auto-generated ID of the inserted prompt.
     */
    @Insert
    suspend fun insertPrompt(prompt: Prompt): Long

    /**
     * Updates an existing prompt.
     * Uses primary key (id) to identify which prompt to update.
     */
    @Update
    suspend fun updatePrompt(prompt: Prompt)

    /**
     * Deletes a prompt by ID.
     */
    @Query("DELETE FROM prompts WHERE id = :promptId")
    suspend fun deletePrompt(promptId: Long)

    /**
     * Deletes all prompts (dangerous operation, use with caution).
     * Backup-critical: This operation should be preceded by backup validation
     */
    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()

    /**
     * Retrieves a single prompt by ID as a Flow for reactive updates.
     * Returns null if prompt not found.
     */
    @Query("SELECT * FROM prompts WHERE id = :promptId LIMIT 1")
    fun getPromptById(promptId: Long): Flow<Prompt?>

    /**
     * Retrieves a single prompt by ID (non-Flow for one-time reads).
     * Used in repository pattern for non-reactive queries.
     */
    @Query("SELECT * FROM prompts WHERE id = :promptId LIMIT 1")
    suspend fun getPromptByIdOnce(promptId: Long): Prompt?

    // ===== List Operations =====

    /**
     * Retrieves all prompts sorted by creation date (newest first).
     * Returns as Flow for reactive updates.
     * Pagination: Use limit and offset for large datasets.
     */
    @Query(
        """
        SELECT * FROM prompts
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getAllPrompts(limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Retrieves all prompts (complete list, use with pagination for large datasets).
     * For galleries with <100 prompts only.
     */
    @Query("SELECT * FROM prompts ORDER BY created_at DESC")
    fun getAllPromptsFlow(): Flow<List<Prompt>>

    /**
     * Counts total number of prompts in database.
     * Used for pagination calculations.
     */
    @Query("SELECT COUNT(*) FROM prompts")
    fun getPromptCount(): Flow<Int>

    // ===== Search Operations =====

    /**
     * Searches prompts by title and content using LIKE pattern matching.
     * Optimization: Pagination + FTS index for scale (PP-T1 mitigation)
     *
     * Search targets both title and content fields.
     * Query is case-insensitive on Android SQLite.
     * Latency target: <300ms for 1000 prompts
     *
     * @param query Search term (will be wrapped with % wildcards)
     * @param limit Results per page (default 50)
     * @param offset Pagination offset (default 0)
     * @return Flow of matching prompts ordered by relevance
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY
            CASE
                WHEN title LIKE :query || '%' THEN 0
                WHEN title LIKE '%' || :query || '%' THEN 1
                ELSE 2
            END,
            created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun searchPrompts(query: String, limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Searches prompts by title only.
     * Faster than full text search when only title matters.
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE title LIKE '%' || :query || '%'
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun searchPromptsByTitle(query: String, limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Searches prompts by content only.
     * Targets the body text of prompts.
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE content LIKE '%' || :query || '%'
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun searchPromptsByContent(query: String, limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    // ===== Filter Operations =====

    /**
     * Retrieves favorite prompts.
     * Used for "Favorites" gallery view.
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE favorite = 1
        ORDER BY updated_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getFavoritePrompts(limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Retrieves prompts by complexity level.
     * Used for filtering gallery by skill level.
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE complexity = :complexity
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getPromptsByComplexity(complexity: String, limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Retrieves prompts filtered by creation date range.
     * Used for date-based filtering in gallery.
     *
     * @param startDateEpochMilli Start date as milliseconds since epoch
     * @param endDateEpochMilli End date as milliseconds since epoch
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE created_at BETWEEN :startDateEpochMilli AND :endDateEpochMilli
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getPromptsByDateRange(
        startDateEpochMilli: Long,
        endDateEpochMilli: Long,
        limit: Int = 50,
        offset: Int = 0
    ): Flow<List<Prompt>>

    // ===== Usage Statistics =====

    /**
     * Increments the usage count for a prompt.
     * Called whenever a user interacts with (views/copies) a prompt.
     */
    @Query("UPDATE prompts SET usage_count = usage_count + 1 WHERE id = :promptId")
    suspend fun incrementUsageCount(promptId: Long)

    /**
     * Updates the last_used timestamp for a prompt.
     * Called whenever a user interacts with a prompt.
     *
     * @param promptId ID of the prompt
     * @param epochMilli Current timestamp in milliseconds since epoch
     */
    @Query("UPDATE prompts SET last_used = :epochMilli WHERE id = :promptId")
    suspend fun updateLastUsedTimestamp(promptId: Long, epochMilli: Long)

    /**
     * Retrieves prompts sorted by usage count (most used first).
     * Used for "Trending" or "Most Used" gallery views.
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE usage_count > 0
        ORDER BY usage_count DESC, last_used DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getMostUsedPrompts(limit: Int = 50, offset: Int = 0): Flow<List<Prompt>>

    /**
     * Retrieves prompts not used recently (older than specified date).
     * Used for cleanup or archival suggestions.
     *
     * @param epochMilli Threshold timestamp in milliseconds since epoch
     */
    @Query(
        """
        SELECT * FROM prompts
        WHERE last_used IS NULL OR last_used < :epochMilli
        ORDER BY created_at DESC
        """
    )
    fun getUnusedPrompts(epochMilli: Long): Flow<List<Prompt>>

    // ===== Favorite Operations =====

    /**
     * Toggles favorite status for a prompt.
     *
     * @param promptId ID of the prompt
     * @param isFavorite New favorite status (true to mark as favorite)
     */
    @Query("UPDATE prompts SET favorite = :isFavorite WHERE id = :promptId")
    suspend fun setFavorite(promptId: Long, isFavorite: Boolean)

    // ===== Batch Operations =====

    /**
     * Inserts multiple prompts in a single transaction.
     * Used for bulk imports (JSON files, etc.).
     * Backup-critical: Used during restore operations
     */
    @Insert
    suspend fun insertPrompts(prompts: List<Prompt>)

    /**
     * Deletes multiple prompts by ID.
     * Used for bulk delete operations.
     */
    @Query("DELETE FROM prompts WHERE id IN (:promptIds)")
    suspend fun deletePrompts(promptIds: List<Long>)

    /**
     * Updates multiple prompts in a single transaction.
     */
    @Update
    suspend fun updatePrompts(prompts: List<Prompt>)
}
