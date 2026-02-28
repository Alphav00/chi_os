package com.promptvault.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.promptvault.android.data.model.MergeRule
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MergeRule entity.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Deliverables: DAO implementation, migration scripts
 *
 * MergeRules are templates for combining multiple prompts.
 * Template syntax: "{prefix}\n\n{body}\n\n{suffix}"
 *
 * All DAOs return Flow<T> for reactive updates.
 * Supports both system (global) and user-created (custom) rules.
 */
@Dao
interface MergeRuleDao {

    // ===== CRUD Operations =====

    /**
     * Inserts a new merge rule into the database.
     * Returns the auto-generated ID of the inserted rule.
     */
    @Insert
    suspend fun insertRule(rule: MergeRule): Long

    /**
     * Updates an existing merge rule.
     * Uses primary key (ruleId) to identify which rule to update.
     */
    @Update
    suspend fun updateRule(rule: MergeRule)

    /**
     * Deletes a merge rule by ID.
     */
    @Query("DELETE FROM merge_rules WHERE ruleId = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    /**
     * Deletes all custom merge rules (preserves system rules with is_global = 1).
     */
    @Query("DELETE FROM merge_rules WHERE is_global = 0")
    suspend fun deleteAllCustomRules()

    /**
     * Retrieves a single merge rule by ID as a Flow.
     */
    @Query("SELECT * FROM merge_rules WHERE ruleId = :ruleId LIMIT 1")
    fun getRuleById(ruleId: Long): Flow<MergeRule?>

    /**
     * Retrieves a single merge rule by ID (non-Flow for one-time reads).
     */
    @Query("SELECT * FROM merge_rules WHERE ruleId = :ruleId LIMIT 1")
    suspend fun getRuleByIdOnce(ruleId: Long): MergeRule?

    // ===== List Operations =====

    /**
     * Retrieves all merge rules (both system and custom).
     * Returns as Flow for reactive updates.
     * Sorted by usage count (most used first), then by name.
     */
    @Query(
        """
        SELECT * FROM merge_rules
        ORDER BY usage_count DESC, name ASC
        """
    )
    fun getAllRules(): Flow<List<MergeRule>>

    /**
     * Counts total number of merge rules in database.
     */
    @Query("SELECT COUNT(*) FROM merge_rules")
    fun getRuleCount(): Flow<Int>

    // ===== Filter Operations =====

    /**
     * Retrieves system (global) merge rules.
     * System rules are predefined and available to all users.
     * Example: "Prompt Fusion", "Intent Combiner", etc.
     */
    @Query(
        """
        SELECT * FROM merge_rules
        WHERE is_global = 1
        ORDER BY usage_count DESC, name ASC
        """
    )
    fun getGlobalRules(): Flow<List<MergeRule>>

    /**
     * Retrieves custom (user-created) merge rules.
     * These are rules created by the user for their own use.
     */
    @Query(
        """
        SELECT * FROM merge_rules
        WHERE is_global = 0
        ORDER BY usage_count DESC, name ASC
        """
    )
    fun getCustomRules(): Flow<List<MergeRule>>

    /**
     * Retrieves merge rules by category.
     * Categories help organize rules (e.g., "system", "custom", "research", etc.).
     */
    @Query(
        """
        SELECT * FROM merge_rules
        WHERE category = :category
        ORDER BY usage_count DESC, name ASC
        """
    )
    fun getRulesByCategory(category: String): Flow<List<MergeRule>>

    /**
     * Searches merge rules by name.
     * Used for rule selection dropdown with search.
     */
    @Query(
        """
        SELECT * FROM merge_rules
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    fun searchRules(query: String): Flow<List<MergeRule>>

    // ===== Usage Statistics =====

    /**
     * Increments the usage count for a merge rule.
     * Called whenever a user executes a merge using this rule.
     */
    @Query("UPDATE merge_rules SET usage_count = usage_count + 1 WHERE ruleId = :ruleId")
    suspend fun incrementUsageCount(ruleId: Long)

    /**
     * Retrieves the most frequently used merge rules.
     * Used for "Suggested Rules" or "Recent Rules" in UI.
     */
    @Query(
        """
        SELECT * FROM merge_rules
        WHERE usage_count > 0
        ORDER BY usage_count DESC
        LIMIT :limit
        """
    )
    fun getMostUsedRules(limit: Int = 10): Flow<List<MergeRule>>

    // ===== Batch Operations =====

    /**
     * Inserts multiple merge rules in a single transaction.
     * Used for bulk initialization of system rules.
     */
    @Insert
    suspend fun insertRules(rules: List<MergeRule>)

    /**
     * Updates multiple merge rules in a single transaction.
     */
    @Update
    suspend fun updateRules(rules: List<MergeRule>)

    /**
     * Deletes multiple merge rules by ID.
     */
    @Query("DELETE FROM merge_rules WHERE ruleId IN (:ruleIds)")
    suspend fun deleteRules(ruleIds: List<Long>)
}
