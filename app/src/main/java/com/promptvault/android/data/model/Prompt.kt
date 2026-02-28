package com.promptvault.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Prompt entity representing a user-created prompt in the local database.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Expertise: Kotlin, Room/SQLite, MVVM architecture
 *
 * Risk References:
 * - PP-T1 (Performance): Full-text search index for search latency <300ms (Milestone 3.5)
 * - CR-001 (Data Loss): Backup-critical entity requiring integrity validation
 */
@Entity(
    tableName = "prompts",
    indices = [
        Index("title", name = "idx_prompt_title"),
        Index("createdAt", name = "idx_prompt_created_at"),
        Index("favorite", name = "idx_prompt_favorite"),
        Index("complexity", name = "idx_prompt_complexity")
    ]
)
data class Prompt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Instant.now(),

    @ColumnInfo(name = "favorite")
    val favorite: Boolean = false,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    @ColumnInfo(name = "last_used")
    val lastUsed: Instant? = null,

    @ColumnInfo(name = "merge_history")
    val mergeHistory: List<MergeRecord> = emptyList(),

    @ColumnInfo(name = "source_language")
    val sourceLanguage: String = "en",

    @ColumnInfo(name = "target_audience")
    val targetAudience: String = "general",

    @ColumnInfo(name = "complexity")
    val complexity: ComplexityLevel = ComplexityLevel.INTERMEDIATE
)

/**
 * Complexity level enum for categorizing prompts.
 * Used for filtering and search optimization.
 */
enum class ComplexityLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

/**
 * Merge record for tracking prompt modification history.
 * Stored as JSON in the database via TypeConverter.
 *
 * Backup-critical: Used for audit trail and recovery (CR-001 mitigation)
 */
data class MergeRecord(
    val sessionId: String,
    val ruleName: String,
    val timestamp: Instant,
    val confidenceScore: Float
)
