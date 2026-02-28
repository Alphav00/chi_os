package com.promptvault.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MergeRule entity representing a template for combining multiple prompts.
 *
 * Template syntax: "{prefix}{body}{suffix}" where variables are substituted
 * from the variables JSON field.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Code Review: All data-layer PRs
 *
 * Risk References:
 * - CR-002 (Injection): Merge rules can be weaponized for jailbreaks
 *   - Use MergeRuleValidator.detectInjectionRisk() before merge execution
 *   - Track dangerous patterns in rule content
 */
@Entity(
    tableName = "merge_rules",
    indices = [
        Index("name", name = "idx_merge_rule_name"),
        Index("is_global", name = "idx_merge_rule_is_global"),
        Index("category", name = "idx_merge_rule_category")
    ]
)
data class MergeRule(
    @PrimaryKey(autoGenerate = true)
    val ruleId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "template")
    val template: String, // "{prefix}\n\n{body}\n\n{suffix}"

    @ColumnInfo(name = "variables")
    val variables: String = "{}", // JSON: { "prefix": "Consider: ", "suffix": "..." }

    @ColumnInfo(name = "is_global")
    val isGlobal: Boolean = false,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    @ColumnInfo(name = "category")
    val category: String = "custom" // "system" or "custom"
)
