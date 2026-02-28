package com.promptvault.android.data.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.promptvault.android.data.model.ComplexityLevel
import com.promptvault.android.data.model.MergeRecord
import java.time.Instant

/**
 * Room TypeConverters for complex types that need JSON serialization.
 *
 * Backend Engineer responsibility (DEVOPS_AND_TEAMS.md):
 * - Data layer, Room schemas, Repository pattern
 * - Expertise: Kotlin, Room/SQLite, MVVM architecture
 *
 * This converter handles:
 * - List<String> for tags and other array fields
 * - List<MergeRecord> for merge history
 * - Instant timestamps
 * - ComplexityLevel enums
 *
 * All conversions are defensive with null-safety and fallback defaults.
 */
class Converters {
    private val gson = Gson()

    // ===== String List Converters =====

    /**
     * Converts List<String> to JSON string for Room storage.
     * Used for: tags, prompt categories, etc.
     */
    @TypeConverter
    fun stringListToJson(value: List<String>?): String {
        if (value == null || value.isEmpty()) {
            return "[]"
        }
        return gson.toJson(value)
    }

    /**
     * Converts JSON string back to List<String>.
     * Defensive: Returns empty list on parse failure.
     */
    @TypeConverter
    fun jsonToStringList(value: String?): List<String> {
        if (value == null || value.isEmpty() || value == "[]") {
            return emptyList()
        }
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            // Defensive: Log and return empty list on parse failure
            System.err.println("Failed to parse string list: $value, error: ${e.message}")
            emptyList()
        }
    }

    // ===== MergeRecord List Converters =====

    /**
     * Converts List<MergeRecord> to JSON string for Room storage.
     * MergeRecord contains: sessionId, ruleName, timestamp, confidenceScore
     * Backup-critical: Used for audit trail (CR-001 mitigation)
     */
    @TypeConverter
    fun mergeRecordListToJson(value: List<MergeRecord>?): String {
        if (value == null || value.isEmpty()) {
            return "[]"
        }
        return gson.toJson(value)
    }

    /**
     * Converts JSON string back to List<MergeRecord>.
     * Defensive: Returns empty list on parse failure.
     * Backup-critical: validate data integrity during restore operations
     */
    @TypeConverter
    fun jsonToMergeRecordList(value: String?): List<MergeRecord> {
        if (value == null || value.isEmpty() || value == "[]") {
            return emptyList()
        }
        return try {
            val type = object : TypeToken<List<MergeRecord>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            System.err.println("Failed to parse merge record list: $value, error: ${e.message}")
            emptyList()
        }
    }

    // ===== Long List Converters =====

    /**
     * Converts List<Long> to JSON string for Room storage.
     * Used for: Collection.promptIds
     * Backup-critical: validate data integrity
     */
    @TypeConverter
    fun longListToJson(value: List<Long>?): String {
        if (value == null || value.isEmpty()) {
            return "[]"
        }
        return gson.toJson(value)
    }

    /**
     * Converts JSON string back to List<Long>.
     * Defensive: Returns empty list on parse failure.
     */
    @TypeConverter
    fun jsonToLongList(value: String?): List<Long> {
        if (value == null || value.isEmpty() || value == "[]") {
            return emptyList()
        }
        return try {
            val type = object : TypeToken<List<Long>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            System.err.println("Failed to parse long list: $value, error: ${e.message}")
            emptyList()
        }
    }

    // ===== Instant Converters =====

    /**
     * Converts Instant to milliseconds since epoch for Room storage.
     * Uses Long for efficient storage and fast queries.
     */
    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    /**
     * Converts milliseconds since epoch back to Instant.
     * Defensive: Returns null for invalid values.
     */
    @TypeConverter
    fun epochMilliToInstant(epochMilli: Long?): Instant? {
        return if (epochMilli == null || epochMilli <= 0) {
            null
        } else {
            try {
                Instant.ofEpochMilli(epochMilli)
            } catch (e: Exception) {
                System.err.println("Failed to parse Instant from epoch milli: $epochMilli, error: ${e.message}")
                null
            }
        }
    }

    // ===== ComplexityLevel Enum Converter =====

    /**
     * Converts ComplexityLevel enum to string for Room storage.
     */
    @TypeConverter
    fun complexityLevelToString(value: ComplexityLevel?): String? {
        return value?.name
    }

    /**
     * Converts string back to ComplexityLevel enum.
     * Defensive: Returns INTERMEDIATE as fallback on invalid input.
     */
    @TypeConverter
    fun stringToComplexityLevel(value: String?): ComplexityLevel {
        if (value == null || value.isEmpty()) {
            return ComplexityLevel.INTERMEDIATE
        }
        return try {
            ComplexityLevel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            System.err.println("Invalid complexity level: $value, defaulting to INTERMEDIATE")
            ComplexityLevel.INTERMEDIATE
        }
    }

    // ===== Generic JSON Converters =====

    /**
     * Generic converter for arbitrary JSON objects stored as strings.
     * Used for: MergeRule.variables, UsageStat.metadata
     */
    @TypeConverter
    fun mapToJson(value: Map<String, Any>?): String {
        if (value == null || value.isEmpty()) {
            return "{}"
        }
        return gson.toJson(value)
    }

    /**
     * Converts JSON string back to Map<String, Any>.
     * Defensive: Returns empty map on parse failure.
     */
    @TypeConverter
    fun jsonToMap(value: String?): Map<String, Any> {
        if (value == null || value.isEmpty() || value == "{}") {
            return emptyMap()
        }
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(value, type) ?: emptyMap()
        } catch (e: Exception) {
            System.err.println("Failed to parse map from JSON: $value, error: ${e.message}")
            emptyMap()
        }
    }
}
