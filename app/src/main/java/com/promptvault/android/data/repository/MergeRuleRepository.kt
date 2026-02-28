package com.promptvault.android.data.repository

import com.promptvault.android.data.dao.MergeRuleDao
import com.promptvault.android.data.model.MergeRule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MergeRuleRepository: Data access abstraction for MergeRule operations.
 *
 * Responsibility: Provide merge rules to MergeViewModel and AutoMergerEngine
 * Owner: Backend/Repository Engineer (ARCHITECTURE.md, Section 3.1)
 *
 * Risk References:
 * - CR-002 (Injection): Merge rules are validated by MergeRuleValidator before use
 * - Deliverable: Repository pattern for data access (Milestone 2.3)
 *
 * Design: Sealed class for type-safe results, suspend functions for async operations.
 */
class MergeRuleRepository @Inject constructor(
    private val mergeRuleDao: MergeRuleDao
) {

    // ===== Read Operations =====

    /**
     * Gets all merge rules as a Flow.
     * Includes both global (system) and custom (user) rules.
     *
     * @return Flow of all available merge rules
     */
    fun getAllRulesFlow(): Flow<List<MergeRule>> {
        return mergeRuleDao.getAllRules()
    }

    /**
     * Gets all global (system-provided) merge rules.
     * These are trusted, pre-validated rules provided by PromptVault.
     *
     * @return Flow of global merge rules
     */
    fun getGlobalRulesFlow(): Flow<List<MergeRule>> {
        return mergeRuleDao.getGlobalRules()
    }

    /**
     * Gets all custom (user-created) merge rules.
     *
     * @return Flow of custom merge rules
     */
    fun getCustomRulesFlow(): Flow<List<MergeRule>> {
        return mergeRuleDao.getCustomRules()
    }

    /**
     * Gets a single merge rule by ID.
     *
     * @param ruleId ID of the merge rule
     * @return The merge rule or null if not found
     */
    suspend fun getRuleById(ruleId: Long): MergeRule? {
        return mergeRuleDao.getRuleById(ruleId)
    }

    /**
     * Gets a random global merge rule.
     * Used for serendipitous merge suggestions (XENOCOGNITIVE_IDEAS.md, Section 4)
     *
     * @return Random global merge rule
     */
    suspend fun getRandomGlobalRule(): MergeRule? {
        return mergeRuleDao.getRandomGlobalRule()
    }

    /**
     * Gets merge rules by category.
     *
     * @param category Category name (e.g., "system", "custom", "creative")
     * @return Flow of rules in that category
     */
    fun getRulesByCategory(category: String): Flow<List<MergeRule>> {
        return mergeRuleDao.getRulesByCategory(category)
    }

    /**
     * Gets most-used merge rules (sorted by usage count).
     *
     * @return Flow of popular merge rules
     */
    fun getMostUsedRulesFlow(): Flow<List<MergeRule>> {
        return mergeRuleDao.getMostUsedRules()
    }

    // ===== Write Operations =====

    /**
     * Saves a new merge rule (insert or update).
     * CR-002 Mitigation: Caller should validate rule with MergeRuleValidator first
     *
     * @param rule The merge rule to save
     * @return ID of the saved rule
     */
    suspend fun saveRule(rule: MergeRule): Long {
        return if (rule.ruleId == 0L) {
            mergeRuleDao.insertRule(rule)
        } else {
            mergeRuleDao.updateRule(rule)
            rule.ruleId
        }
    }

    /**
     * Inserts a new merge rule.
     *
     * @param rule The merge rule to insert
     * @return ID of the newly inserted rule
     */
    suspend fun insertRule(rule: MergeRule): Long {
        return mergeRuleDao.insertRule(rule)
    }

    /**
     * Updates an existing merge rule.
     *
     * @param rule The merge rule to update (identified by ruleId)
     */
    suspend fun updateRule(rule: MergeRule) {
        mergeRuleDao.updateRule(rule)
    }

    /**
     * Increments the usage count for a merge rule.
     * Called after successful merge execution.
     *
     * @param ruleId ID of the rule
     */
    suspend fun incrementUsageCount(ruleId: Long) {
        mergeRuleDao.incrementUsageCount(ruleId)
    }

    /**
     * Deletes a merge rule by ID.
     *
     * @param ruleId ID of the rule to delete
     */
    suspend fun deleteRule(ruleId: Long) {
        mergeRuleDao.deleteRule(ruleId)
    }

    /**
     * Deletes multiple merge rules in a single transaction.
     *
     * @param ruleIds List of rule IDs to delete
     */
    suspend fun deleteRules(ruleIds: List<Long>) {
        mergeRuleDao.deleteRules(ruleIds)
    }

    // ===== Utility Operations =====

    /**
     * Gets total count of merge rules.
     *
     * @return Flow emitting total rule count
     */
    fun getRuleCount(): Flow<Int> {
        return mergeRuleDao.getRuleCount()
    }

    /**
     * Checks if a rule exists by ID.
     *
     * @param ruleId ID of the rule
     * @return True if rule exists, false otherwise
     */
    suspend fun ruleExists(ruleId: Long): Boolean {
        return getRuleById(ruleId) != null
    }
}
