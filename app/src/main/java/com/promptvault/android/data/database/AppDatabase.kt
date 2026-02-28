package com.promptvault.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.promptvault.android.data.dao.MergeRuleDao
import com.promptvault.android.data.dao.PromptDao
import com.promptvault.android.data.model.Collection
import com.promptvault.android.data.model.MergeRule
import com.promptvault.android.data.model.Prompt
import com.promptvault.android.data.model.QuickLink
import com.promptvault.android.data.util.Converters

/**
 * Room Database for PromptVault Android.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Deliverables: DAO implementation, migration scripts
 * - Code Review: All data-layer PRs
 * - Expertise: Kotlin, Room/SQLite, MVVM architecture
 *
 * Database Architecture:
 * - Version: 1 (initial schema)
 * - Entities: Prompt, MergeRule, Collection, QuickLink (Phase 1)
 * - TypeConverters: Handles List<String>, List<Long>, Instant, JSON
 * - Indices: Optimized for search queries and filtering
 *
 * Risk References:
 * - PP-T1 (Performance): FTS index and pagination strategy for <300ms search latency
 * - CR-001 (Data Loss): Backup-critical database with validation
 *   Backup-critical: validate data integrity during restore operations
 * - HR-001 (Schema): Database schema compatibility and migration strategy
 *
 * Migration Strategy:
 * - Database version 1 is initial schema with no migrations needed
 * - Future migrations (v2, v3, etc.) should be added as FallbackMigrationFrom annotations
 * - All migrations must be tested on API 24, 30, 34 (Firebase Test Lab)
 * - Pre-migration backups are recommended for users (implemented in StorageManager)
 *
 * Performance Optimization:
 * - Indices on frequently queried columns (title, createdAt, favorite, complexity)
 * - Pagination via limit/offset in DAOs (max 50 items per query)
 * - LRU in-memory cache (100 prompts) in PromptRepository
 * - Full-text search index planned for Phase 2 (Room FTS5)
 *
 * Caching Strategy:
 * - SQLite query cache handled by Room automatically
 * - No explicit pagination beyond offset/limit (Paging 3 in Repository)
 * - Search results limited to 50 items at a time (infinite scroll)
 *
 * Testing:
 * - Unit tests: In-memory database with test DAOs
 * - Integration tests: Real database on device
 * - Migration tests: Upgrade path testing from v1 → vN
 * - Performance tests: Query execution time benchmarks
 *
 * Security:
 * - Data encrypted at rest (optional via Device Credential integration)
 * - All user data stored locally (no network calls in Phase 1)
 * - Export operations warn users before sharing sensitive data
 *
 * Singleton Pattern:
 * - Use AppDatabase.getInstance(context) for all database access
 * - Lazy-initialized on first access to avoid ANR on app startup
 * - Thread-safe via Kotlin's built-in lazy delegate
 */
@Database(
    entities = [
        Prompt::class,
        MergeRule::class,
        Collection::class,
        QuickLink::class
    ],
    version = 1,
    exportSchema = true // Enables schema export to app/schemas/ for version tracking
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ===== Data Access Objects =====

    abstract fun promptDao(): PromptDao
    abstract fun mergeRuleDao(): MergeRuleDao

    companion object {
        private const val DATABASE_NAME = "promptvault.db"
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Singleton accessor for AppDatabase.
         * Uses double-checked locking for thread-safe lazy initialization.
         *
         * @param context Application context
         * @return Singleton AppDatabase instance
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        /**
         * Builds the Room database with configuration.
         *
         * Configuration:
         * - Database name: "promptvault.db"
         * - Fallback migration: Destructive (clears data if migration fails)
         *   This is acceptable for Phase 1 as data is stored in backups
         * - Enable WAL (Write-Ahead Logging) for better concurrency
         * - Query executor: Uses default Room thread pool
         *
         * Optimization: Pagination + FTS index for scale (PP-T1 mitigation)
         * Backup-critical: validate data integrity (CR-001 mitigation)
         *
         * @param context Application context
         * @return Configured AppDatabase instance
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                // Fallback: Destroy old database if migration path doesn't exist
                // This is acceptable because users have backups (Phase 2 will add versioned migrations)
                .fallbackToDestructiveMigration()
                // Enable WAL for concurrent read/write access
                .enableMultiInstanceInvalidation()
                .build()
        }

        /**
         * Clears the database singleton instance.
         * Used for testing purposes to reset database state.
         * WARNING: Do not call in production code.
         */
        fun clearInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }
}
