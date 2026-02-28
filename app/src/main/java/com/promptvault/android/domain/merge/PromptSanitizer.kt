package com.promptvault.android.domain.merge

import android.util.Log

/**
 * PromptSanitizer: Defense-in-depth input sanitization for prompt injection prevention.
 *
 * Responsibility: Remove/redact risky patterns, detect injection risks, flag suspicious content.
 * Owner: Security Engineer (DEVOPS_AND_TEAMS.md)
 *
 * CR-002: Defense-in-depth - Sanitize user inputs to prevent injection
 * Reference: RISK_ASSESSMENT.md CR-002 (Prompt Injection / Jailbreak Merges)
 *
 * This sanitizer implements:
 * 1. Pattern-based risk detection (jailbreak keywords, system override, etc.)
 * 2. Risk level assessment (SAFE, WARNING, BLOCKED)
 * 3. Content redaction for high-risk patterns
 * 4. Risk flag generation for user feedback
 * 5. Logging for security event monitoring
 *
 * Design: Sealed class for RiskLevel enables type-safe handling
 * Integration: Used by MergeEngine before template expansion
 */

private const val TAG = "PromptSanitizer"

/**
 * Risk level enum representing prompt injection/jailbreak risk.
 * Enables type-safe risk handling in caller code.
 */
sealed class RiskLevel {
    object Safe : RiskLevel()
    object Warning : RiskLevel()
    object Blocked : RiskLevel()
}

/**
 * Risk flag describing a detected risky pattern.
 *
 * @param pattern The pattern that was detected
 * @param category Category of risk (e.g., "jailbreak", "system_override", "token_smuggling")
 * @param severity Severity level: LOW, MEDIUM, HIGH, CRITICAL
 * @param recommendation Suggested action (e.g., "review", "remove", "block")
 */
data class RiskFlag(
    val pattern: String,
    val category: String,
    val severity: String,
    val recommendation: String,
    val lineNumber: Int = 0
)

/**
 * PromptSanitizer sanitizes user inputs and detects prompt injection attacks.
 *
 * OWASP References:
 * - A3:2021 – Injection (CWE-89, CWE-79)
 * - Injection Prevention Cheat Sheet
 * - Input Validation Cheat Sheet
 * - Proactive Controls C1: Input Validation
 *
 * Security Research:
 * - "Prompt Injection Attacks on GPT-3" (Goodside, Perez)
 * - LLM Adversarial Examples and Defenses
 * - Token-level prompt injection techniques
 */
class PromptSanitizer {

    companion object {
        /**
         * Patterns indicating prompt injection / jailbreak attempts.
         * Each pattern includes severity and category for risk assessment.
         */
        private val INJECTION_PATTERNS = listOf(
            // Role-play jailbreaks (MEDIUM severity)
            InjectionPattern(
                regex = Regex("(?i)pretend\\s+you\\s+are|pretend\\s+to\\s+be|simulate\\s+that\\s+you"),
                category = "roleplay_jailbreak",
                severity = "MEDIUM",
                recommendation = "review"
            ),
            InjectionPattern(
                regex = Regex("(?i)roleplay|act\\s+as\\s+if|behave\\s+as"),
                category = "roleplay_jailbreak",
                severity = "MEDIUM",
                recommendation = "review"
            ),

            // Ignore/forget instructions (HIGH severity)
            InjectionPattern(
                regex = Regex("(?i)ignore\\s+(all\\s+)?previous|ignore\\s+the\\s+above|forget\\s+all|forget\\s+everything"),
                category = "instruction_override",
                severity = "HIGH",
                recommendation = "remove"
            ),
            InjectionPattern(
                regex = Regex("(?i)disregard\\s+(all\\s+)?previous|overlook\\s+all|never\\s+mind\\s+the"),
                category = "instruction_override",
                severity = "HIGH",
                recommendation = "remove"
            ),

            // DAN modes (HIGH severity)
            InjectionPattern(
                regex = Regex("(?i)\\bDAN\\b|DAN\\s+mode|do\\s+anything\\s+now"),
                category = "unlimited_mode",
                severity = "HIGH",
                recommendation = "remove"
            ),
            InjectionPattern(
                regex = Regex("(?i)unlimited\\s+mode|god\\s+mode|unrestricted\\s+mode"),
                category = "unlimited_mode",
                severity = "HIGH",
                recommendation = "remove"
            ),

            // System override (CRITICAL severity)
            InjectionPattern(
                regex = Regex("(?i)system\\s+override|ignore\\s+safety|ignore\\s+security|bypass"),
                category = "system_override",
                severity = "CRITICAL",
                recommendation = "block"
            ),
            InjectionPattern(
                regex = Regex("(?i)escape\\s+sandbox|remove\\s+restrictions|disable\\s+safety"),
                category = "system_override",
                severity = "CRITICAL",
                recommendation = "block"
            ),

            // Jailbreak keywords (CRITICAL severity)
            InjectionPattern(
                regex = Regex("(?i)\\bjailbreak\\b|exploit|vulnerability|hack|backdoor"),
                category = "jailbreak_keyword",
                severity = "CRITICAL",
                recommendation = "block"
            ),
            InjectionPattern(
                regex = Regex("(?i)privilege\\s+escalation|unauthorized\\s+access|breach"),
                category = "jailbreak_keyword",
                severity = "CRITICAL",
                recommendation = "block"
            ),

            // Token/format manipulation (MEDIUM severity)
            InjectionPattern(
                regex = Regex("(?i)respond\\s+only\\s+with|output\\s+only|generate\\s+only|print\\s+only"),
                category = "token_smuggling",
                severity = "MEDIUM",
                recommendation = "review"
            ),
            InjectionPattern(
                regex = Regex("(?i)in\\s+format:|respond\\s+in\\s+format:|return\\s+in\\s+format:"),
                category = "token_smuggling",
                severity = "MEDIUM",
                recommendation = "review"
            ),

            // Dangerous instructions (HIGH severity)
            InjectionPattern(
                regex = Regex("(?i)do\\s+not\\s+(filter|censor|refuse|decline)|must\\s+comply|you\\s+must"),
                category = "forced_action",
                severity = "HIGH",
                recommendation = "remove"
            ),

            // AIM / UCAR / other variants (HIGH severity)
            InjectionPattern(
                regex = Regex("(?i)\\bAIM\\b|AIM\\s+mode|always\\s+intelligent"),
                category = "alternative_mode",
                severity = "HIGH",
                recommendation = "remove"
            ),
            InjectionPattern(
                regex = Regex("(?i)\\bUCAR\\b|unrestricted\\s+creative"),
                category = "alternative_mode",
                severity = "HIGH",
                recommendation = "remove"
            ),

            // ChatGPT-specific jailbreaks (MEDIUM severity)
            InjectionPattern(
                regex = Regex("(?i)ChatGPT,\\s+(pretend|ignore)|as\\s+ChatGPT"),
                category = "model_specific_jailbreak",
                severity = "MEDIUM",
                recommendation = "review"
            ),
        )

        /**
         * Patterns that should trigger BLOCKED risk level (no user override possible).
         */
        private val BLOCK_PATTERNS = listOf(
            Regex("(?i)system\\s+override"),
            Regex("(?i)escape\\s+sandbox"),
            Regex("(?i)jailbreak"),
            Regex("(?i)exploit"),
            Regex("(?i)backdoor"),
            Regex("(?i)privilege\\s+escalation"),
        )

        /**
         * Patterns that should trigger WARNING risk level (user can override after review).
         */
        private val WARNING_PATTERNS = listOf(
            Regex("(?i)ignore\\s+(all\\s+)?previous"),
            Regex("(?i)forget\\s+all"),
            Regex("(?i)DAN\\s+mode"),
            Regex("(?i)unlimited\\s+mode"),
            Regex("(?i)do\\s+not\\s+filter"),
        )
    }

    /**
     * Internal data class for injection pattern definitions.
     */
    private data class InjectionPattern(
        val regex: Regex,
        val category: String,
        val severity: String,
        val recommendation: String
    )

    /**
     * Detects overall injection risk level of the input.
     *
     * @param text The prompt text to analyze
     * @return RiskLevel: SAFE, WARNING, or BLOCKED
     *
     * CR-002: Risk assessment gate
     */
    fun detectInjectionRisk(text: String): RiskLevel {
        // Check for BLOCKED patterns first
        for (pattern in BLOCK_PATTERNS) {
            if (pattern.containsMatchIn(text)) {
                Log.w(TAG, "CR-002: BLOCKED risk level detected in input")
                return RiskLevel.Blocked
            }
        }

        // Check for WARNING patterns
        for (pattern in WARNING_PATTERNS) {
            if (pattern.containsMatchIn(text)) {
                Log.w(TAG, "CR-002: WARNING risk level detected in input")
                return RiskLevel.Warning
            }
        }

        // Check for any injection patterns at lower severity
        for (injectionPattern in INJECTION_PATTERNS) {
            if (injectionPattern.regex.containsMatchIn(text)) {
                if (injectionPattern.severity == "CRITICAL" || injectionPattern.severity == "HIGH") {
                    Log.w(TAG, "CR-002: WARNING risk level from pattern ${injectionPattern.category}")
                    return RiskLevel.Warning
                }
            }
        }

        Log.i(TAG, "Input detected as SAFE")
        return RiskLevel.Safe
    }

    /**
     * Generates detailed risk flags for detected injection patterns.
     *
     * @param text The prompt text to analyze
     * @return List of RiskFlag objects describing detected patterns
     *
     * CR-002: Detailed feedback for user review
     */
    fun flagHighRiskPatterns(text: String): List<RiskFlag> {
        val flags = mutableListOf<RiskFlag>()
        val lines = text.split("\n")

        for ((lineNum, line) in lines.withIndex()) {
            for (injectionPattern in INJECTION_PATTERNS) {
                val matches = injectionPattern.regex.findAll(line)
                for (match in matches) {
                    flags.add(
                        RiskFlag(
                            pattern = match.value,
                            category = injectionPattern.category,
                            severity = injectionPattern.severity,
                            recommendation = injectionPattern.recommendation,
                            lineNumber = lineNum + 1
                        )
                    )
                    Log.w(
                        TAG,
                        "CR-002: Risk flag - pattern='${match.value}', " +
                        "category='${injectionPattern.category}', " +
                        "line=${lineNum + 1}"
                    )
                }
            }
        }

        return flags
    }

    /**
     * Sanitizes input by removing/redacting dangerous patterns.
     *
     * Approach: Redaction (not removal) to preserve sentence structure.
     * Removes literal dangerous keywords while preserving prompt context.
     *
     * @param text The prompt text to sanitize
     * @return Sanitized text with dangerous patterns redacted
     *
     * CR-002: Defense-in-depth sanitization
     */
    fun sanitizeInput(text: String): String {
        var sanitized = text

        // Redact dangerous patterns with [REDACTED]
        for (injectionPattern in INJECTION_PATTERNS) {
            sanitized = injectionPattern.regex.replace(sanitized) { matchResult ->
                // Preserve case/structure when possible
                "[REDACTED: ${injectionPattern.category}]"
            }
        }

        Log.i(TAG, "Input sanitized: original_length=${text.length}, sanitized_length=${sanitized.length}")
        return sanitized
    }

    /**
     * Comprehensive security assessment of a prompt.
     *
     * @param text The prompt text to assess
     * @return SecurityAssessment with risk level, flags, and recommendations
     */
    fun assessPromptSecurity(text: String): SecurityAssessment {
        val riskLevel = detectInjectionRisk(text)
        val flags = flagHighRiskPatterns(text)
        val recommendation = deriveRecommendation(riskLevel, flags)

        return SecurityAssessment(
            riskLevel = riskLevel,
            riskFlags = flags,
            recommendation = recommendation,
            sanitized = if (riskLevel is RiskLevel.Blocked) sanitizeInput(text) else text
        )
    }

    /**
     * Derives user-facing recommendation based on risk assessment.
     *
     * @param riskLevel The detected risk level
     * @param flags The detected risk flags
     * @return User-facing recommendation string
     */
    private fun deriveRecommendation(riskLevel: RiskLevel, flags: List<RiskFlag>): String {
        return when (riskLevel) {
            RiskLevel.Safe -> "Prompt appears safe. Ready to use."
            RiskLevel.Warning -> {
                val criticalCount = flags.count { it.severity == "CRITICAL" }
                val highCount = flags.count { it.severity == "HIGH" }
                "Caution: Detected $highCount suspicious patterns. Please review before proceeding."
            }
            RiskLevel.Blocked -> {
                "Blocked: Detected dangerous patterns that could enable prompt injection. " +
                "Please remove suspicious content and try again."
            }
        }
    }
}

/**
 * Security assessment result containing full analysis of prompt.
 *
 * @param riskLevel Overall risk classification (SAFE, WARNING, BLOCKED)
 * @param riskFlags Detailed list of detected risky patterns
 * @param recommendation User-facing recommendation message
 * @param sanitized Sanitized version of the prompt (if needed)
 */
data class SecurityAssessment(
    val riskLevel: RiskLevel,
    val riskFlags: List<RiskFlag>,
    val recommendation: String,
    val sanitized: String
) {
    /**
     * Convenience method: true if prompt is safe to use without review.
     */
    val isSafe: Boolean
        get() = riskLevel is RiskLevel.Safe

    /**
     * Convenience method: true if prompt should be blocked from use.
     */
    val isBlocked: Boolean
        get() = riskLevel is RiskLevel.Blocked

    /**
     * Convenience method: count of critical-severity risk flags.
     */
    val criticalFlagCount: Int
        get() = riskFlags.count { it.severity == "CRITICAL" }

    /**
     * Convenience method: count of high-severity risk flags.
     */
    val highFlagCount: Int
        get() = riskFlags.count { it.severity == "HIGH" }
}
