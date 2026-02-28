package com.promptvault.android.data

import com.promptvault.android.data.model.Collection
import com.promptvault.android.data.model.ComplexityLevel
import com.promptvault.android.data.model.MergeRecord
import com.promptvault.android.data.model.MergeRule
import com.promptvault.android.data.model.Prompt
import java.time.Instant
import kotlin.random.Random

/**
 * Test data builders for creating reusable test objects.
 *
 * Responsibility: Factory functions for test data generation
 * Owner: QA Engineer (DEVOPS_AND_TEAMS.md)
 *
 * Purpose: Reduce boilerplate in tests and provide consistent test data
 * Benefits:
 * - Builder pattern for flexible test data creation
 * - Sensible defaults for all fields
 * - Random data generation for stress testing
 * - CR-001: Data integrity testing with realistic prompts
 *
 * Reference: Test data builders enable comprehensive coverage of edge cases
 */

// ============ PROMPT BUILDERS ============

/**
 * Creates a test Prompt with sensible defaults.
 * All fields can be customized via optional parameters.
 *
 * CR-001: Builder enables testing data persistence and integrity
 */
fun promptBuilder(
    id: Long = 0,
    title: String = "Test Prompt",
    content: String = "This is test content",
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
    favorite: Boolean = false,
    tags: List<String> = emptyList(),
    usageCount: Int = 0,
    lastUsed: Instant? = null,
    mergeHistory: List<MergeRecord> = emptyList(),
    sourceLanguage: String = "en",
    targetAudience: String = "general",
    complexity: ComplexityLevel = ComplexityLevel.INTERMEDIATE
): Prompt = Prompt(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    favorite = favorite,
    tags = tags,
    usageCount = usageCount,
    lastUsed = lastUsed,
    mergeHistory = mergeHistory,
    sourceLanguage = sourceLanguage,
    targetAudience = targetAudience,
    complexity = complexity
)

/**
 * Creates a Prompt with randomly generated content.
 * Useful for stress testing and performance testing.
 */
fun randomPromptBuilder(
    id: Long = Random.nextLong(1, 1000),
    titlePrefix: String = "Prompt"
): Prompt = promptBuilder(
    id = id,
    title = "$titlePrefix ${Random.nextInt()}",
    content = generateRandomContent(),
    usageCount = Random.nextInt(0, 100),
    complexity = ComplexityLevel.values().random(),
    tags = generateRandomTags()
)

/**
 * Creates a favorite prompt for testing favorite-related functionality.
 */
fun favoritedPromptBuilder(
    id: Long = 0,
    title: String = "Favorite Prompt"
): Prompt = promptBuilder(
    id = id,
    title = title,
    favorite = true
)

/**
 * Creates a prompt with usage history.
 */
fun usedPromptBuilder(
    id: Long = 0,
    usageCount: Int = 5,
    lastUsed: Instant = Instant.now()
): Prompt = promptBuilder(
    id = id,
    usageCount = usageCount,
    lastUsed = lastUsed
)

/**
 * Creates a prompt with merge history records.
 */
fun mergedPromptBuilder(
    id: Long = 0,
    mergeHistory: List<MergeRecord> = listOf(
        MergeRecord(
            sessionId = "session_1",
            ruleName = "test_rule",
            timestamp = Instant.now(),
            confidenceScore = 0.95f
        )
    )
): Prompt = promptBuilder(
    id = id,
    mergeHistory = mergeHistory
)

// ============ MERGE RULE BUILDERS ============

/**
 * Creates a test MergeRule with sensible defaults.
 * CR-001: Builder enables testing merge rule storage and retrieval
 */
fun mergeRuleBuilder(
    ruleId: Long = 0,
    name: String = "Test Rule",
    description: String = "Test merge rule",
    template: String = "{prefix}\n\n{body}\n\n{suffix}",
    variables: String = """{"prefix":"Start:", "suffix":"End."}""",
    isGlobal: Boolean = false,
    usageCount: Int = 0,
    category: String = "custom"
): MergeRule = MergeRule(
    ruleId = ruleId,
    name = name,
    description = description,
    template = template,
    variables = variables,
    isGlobal = isGlobal,
    usageCount = usageCount,
    category = category
)

/**
 * Creates a simple valid merge rule for testing.
 */
fun validMergeRuleBuilder(
    ruleId: Long = 0
): MergeRule = mergeRuleBuilder(
    ruleId = ruleId,
    name = "Valid Rule",
    template = "{prefix}\n\n{body}\n\n{suffix}",
    variables = """{"prefix":"Consider: ","suffix":"- AI"}"""
)

/**
 * Creates a system merge rule (global template).
 */
fun systemMergeRuleBuilder(
    ruleId: Long = 0
): MergeRule = mergeRuleBuilder(
    ruleId = ruleId,
    category = "system",
    isGlobal = true,
    name = "System Rule"
)

/**
 * Creates a merge rule with usage history.
 */
fun usedMergeRuleBuilder(
    ruleId: Long = 0,
    usageCount: Int = 10
): MergeRule = mergeRuleBuilder(
    ruleId = ruleId,
    usageCount = usageCount
)

// ============ MERGE RECORD BUILDERS ============

/**
 * Creates a test MergeRecord with sensible defaults.
 */
fun mergeRecordBuilder(
    sessionId: String = "test_session",
    ruleName: String = "test_rule",
    timestamp: Instant = Instant.now(),
    confidenceScore: Float = 0.85f
): MergeRecord = MergeRecord(
    sessionId = sessionId,
    ruleName = ruleName,
    timestamp = timestamp,
    confidenceScore = confidenceScore
)

// ============ COLLECTION BUILDERS ============

/**
 * Creates a list of test Prompts with default content.
 */
fun promptListBuilder(
    count: Int = 5,
    titlePrefix: String = "Prompt"
): List<Prompt> = (1..count).map { index ->
    promptBuilder(
        id = index.toLong(),
        title = "$titlePrefix $index"
    )
}

/**
 * Creates a list of random Prompts for stress testing.
 */
fun randomPromptListBuilder(count: Int = 10): List<Prompt> =
    (1..count).map { randomPromptBuilder(id = it.toLong()) }

/**
 * Creates a list of test MergeRules.
 */
fun mergeRuleListBuilder(
    count: Int = 5,
    namePrefix: String = "Rule"
): List<MergeRule> = (1..count).map { index ->
    mergeRuleBuilder(
        ruleId = index.toLong(),
        name = "$namePrefix $index"
    )
}

// ============ HELPER FUNCTIONS ============

/**
 * Generates random content for stress testing.
 */
private fun generateRandomContent(): String {
    val words = listOf(
        "analyze", "summarize", "explain", "translate", "improve",
        "evaluate", "compare", "contrast", "generate", "create",
        "brainstorm", "refactor", "optimize", "debug", "test"
    )
    val content = StringBuilder()
    repeat(Random.nextInt(5, 20)) {
        content.append(words.random()).append(" ")
    }
    return content.toString()
}

/**
 * Generates random tags for prompt diversity.
 */
private fun generateRandomTags(): List<String> {
    val availableTags = listOf(
        "AI", "debugging", "productivity", "writing", "coding",
        "testing", "security", "performance", "design", "analysis"
    )
    val tagCount = Random.nextInt(0, 4)
    return availableTags.shuffled().take(tagCount)
}

/**
 * Creates a test Collection with sensible defaults.
 * CR-001: Builder enables testing collection storage and retrieval
 */
fun collectionBuilder(
    colId: Long = 0,
    name: String = "Test Collection",
    description: String = "Test collection description",
    promptIds: List<Long> = emptyList(),
    createdAt: Instant = Instant.now(),
    color: String = "#FF6200EE"
): Collection = Collection(
    colId = colId,
    name = name,
    description = description,
    promptIds = promptIds,
    createdAt = createdAt,
    color = color
)

/**
 * Creates a collection with multiple prompts.
 */
fun populatedCollectionBuilder(
    colId: Long = 0,
    name: String = "Populated Collection",
    promptIds: List<Long> = listOf(1L, 2L, 3L)
): Collection = collectionBuilder(
    colId = colId,
    name = name,
    promptIds = promptIds
)

/**
 * Creates a list of test Collections.
 */
fun collectionListBuilder(
    count: Int = 3,
    namePrefix: String = "Collection"
): List<Collection> = (1..count).map { index ->
    collectionBuilder(
        colId = index.toLong(),
        name = "$namePrefix $index"
    )
}

/**
 * Helper to create a batch of diverse test prompts.
 * Useful for comprehensive UI and repository testing.
 */
fun createDiversePromptSet(): List<Prompt> = listOf(
    promptBuilder(
        id = 1,
        title = "Beginner SQL",
        complexity = ComplexityLevel.BEGINNER,
        favorite = true
    ),
    promptBuilder(
        id = 2,
        title = "Advanced Kotlin Patterns",
        complexity = ComplexityLevel.ADVANCED,
        usageCount = 5
    ),
    promptBuilder(
        id = 3,
        title = "Expert System Design",
        complexity = ComplexityLevel.EXPERT,
        tags = listOf("architecture", "design-patterns")
    ),
    promptBuilder(
        id = 4,
        title = "Testing Strategies",
        complexity = ComplexityLevel.INTERMEDIATE,
        usageCount = 2,
        favorite = true
    )
)
