package com.promptvault.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * QuickLink entity representing a deep link shortcut to a prompt or collection.
 *
 * QuickLinks enable users to access frequently used prompts or collections
 * directly via deep links (e.g., from widgets or external apps).
 *
 * Deep link URI format: "promptvault://v2/prompt/{id}" or "promptvault://v2/collection/{colId}"
 * Version prefix ensures backward compatibility during app updates.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 *
 * Risk References:
 * - MR-001 (DeepLinks): Quick links can break after app updates
 *   Solution: Version deep link URI scheme with migration logic in DeepLinkHandler
 */
@Entity(
    tableName = "quick_links",
    indices = [
        Index("name", name = "idx_quick_link_name"),
        Index("target_prompt_id", name = "idx_quick_link_prompt"),
        Index("target_col_id", name = "idx_quick_link_collection"),
        Index("sort_order", name = "idx_quick_link_sort_order")
    ]
)
data class QuickLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "deep_link_uri")
    val deepLinkUri: String, // "promptvault://v2/prompt/{id}" or "promptvault://v2/collection/{colId}"

    @ColumnInfo(name = "uri_version")
    val uriVersion: Int = 2, // Deep link schema version for backward compatibility

    @ColumnInfo(name = "target_prompt_id")
    val targetPromptId: Long? = null,

    @ColumnInfo(name = "target_col_id")
    val targetColId: Long? = null,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now()
)
