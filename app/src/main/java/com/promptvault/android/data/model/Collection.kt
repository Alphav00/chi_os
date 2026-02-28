package com.promptvault.android.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Collection entity representing a user-organized group of prompts.
 *
 * Collections are containers for organizing related prompts together.
 * The promptIds field stores a JSON array of prompt IDs.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Deliverables: DAO implementation, migration scripts
 *
 * Risk References:
 * - CR-001 (Data Loss): Backup-critical entity
 *   Backup-critical: validate data integrity during restore operations
 */
@Entity(
    tableName = "collections",
    indices = [
        Index("name", name = "idx_collection_name"),
        Index("created_at", name = "idx_collection_created_at")
    ]
)
data class Collection(
    @PrimaryKey(autoGenerate = true)
    val colId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "prompt_ids")
    val promptIds: List<Long> = emptyList(), // Stored as JSON array

    @ColumnInfo(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @ColumnInfo(name = "color")
    val color: String = "#FF6200EE" // Material Design purple
)
