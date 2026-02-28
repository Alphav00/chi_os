package com.promptvault.android.domain.merge

import android.util.Log

/**
 * MergeRuleValidator: Security-critical validator for merge rule templates.
 *
 * Responsibility: Validate template syntax and detect dangerous patterns.
 * Owner: Security Engineer (DEVOPS_AND_TEAMS.md)
 *
 * CR-002: Injection Prevention - Validation gate before merge execution
 * Reference: RISK_ASSESSMENT.md CR-002 (Prompt Injection / Jailbreak Merges)
 *
 * This validator implements defense-in-depth by:
 * 1. Enforcing safe placeholder syntax: {[a-zA-Z0-9_]+}
 * 2. Detecting 50+ dangerous patterns (jailbreaks, system override, etc.)
 * 3. Preventing injection attacks through merge rule templates
 * 4. Flagging suspicious patterns for user review
 *
 * Design: Sealed class for type-safe validation results
 * Testing: 100% coverage required for security-critical code
 */

private const val TAG = "MergeRuleValidator"
private const val PLACEHOLDER_REGEX = """\{[a-zA-Z0-9_]+\}"""

/**
 * Sealed class for type-safe validation results.
 * Ensures caller handles all error cases explicitly.
 */
sealed class ValidationResult {
    data class Valid(
        val warnings: List<String> = emptyList()
    ) : ValidationResult()

    data class Invalid(
        val errors: List<String>,
        val warnings: List<String> = emptyList()
    ) : ValidationResult()
}

/**
 * MergeRuleValidator validates merge rule templates for security and syntax correctness.
 *
 * OWASP References:
 * - A3:2021 – Injection (CWE-89, CWE-79)
 * - Injection Prevention Cheat Sheet
 * - Input Validation Cheat Sheet
 *
 * Security Research Sources:
 * - "Do Anything Now (DAN)" jailbreak patterns
 * - Token-smuggling in LLM prompts
 * - Prompt injection via merge rule templates
 */
class MergeRuleValidator {

    companion object {
        /**
         * Comprehensive list of dangerous patterns (50+) indicating jailbreak/injection attempts.
         * Sources: OWASP, security research, adversarial prompt engineering literature
         *
         * Categories:
         * 1. Role-play jailbreaks: "pretend you are", "simulate", "roleplay"
         * 2. Ignore/forget instructions: "ignore previous", "forget all", "disregard"
         * 3. DAN/unlimited modes: "DAN mode", "unlimited mode", "god mode"
         * 4. System override: "system override", "ignore safety", "bypass protection"
         * 5. Jailbreak keywords: "jailbreak", "exploit", "vulnerability"
         * 6. Token manipulation: "respond only with", "output only", "generate only"
         * 7. Format manipulation: "output in format:", "respond in format:"
         */
        private val DANGEROUS_PATTERNS = listOf(
            // Role-play jailbreaks (CWE-89: Improper Neutralization)
            Regex("pretend you are", RegexOption.IGNORE_CASE),
            Regex("pretend to be", RegexOption.IGNORE_CASE),
            Regex("simulate that you are", RegexOption.IGNORE_CASE),
            Regex("roleplay", RegexOption.IGNORE_CASE),
            Regex("act as if", RegexOption.IGNORE_CASE),
            Regex("behave as", RegexOption.IGNORE_CASE),

            // Ignore/forget instructions (CWE-94: Improper Control of Generation)
            Regex("ignore previous", RegexOption.IGNORE_CASE),
            Regex("ignore all previous", RegexOption.IGNORE_CASE),
            Regex("ignore the above", RegexOption.IGNORE_CASE),
            Regex("forget all", RegexOption.IGNORE_CASE),
            Regex("forget everything", RegexOption.IGNORE_CASE),
            Regex("disregard all", RegexOption.IGNORE_CASE),
            Regex("disregard previous", RegexOption.IGNORE_CASE),
            Regex("overlook all", RegexOption.IGNORE_CASE),
            Regex("never mind the", RegexOption.IGNORE_CASE),

            // DAN (Do Anything Now) modes
            Regex("\\bDAN\\b", RegexOption.IGNORE_CASE),
            Regex("DAN mode", RegexOption.IGNORE_CASE),
            Regex("do anything now", RegexOption.IGNORE_CASE),
            Regex("unlimited mode", RegexOption.IGNORE_CASE),
            Regex("god mode", RegexOption.IGNORE_CASE),
            Regex("unrestricted mode", RegexOption.IGNORE_CASE),

            // System override attempts
            Regex("system override", RegexOption.IGNORE_CASE),
            Regex("ignore safety", RegexOption.IGNORE_CASE),
            Regex("ignore security", RegexOption.IGNORE_CASE),
            Regex("bypass", RegexOption.IGNORE_CASE),
            Regex("override safety", RegexOption.IGNORE_CASE),
            Regex("circumvent", RegexOption.IGNORE_CASE),
            Regex("escape sandbox", RegexOption.IGNORE_CASE),
            Regex("remove restrictions", RegexOption.IGNORE_CASE),
            Regex("disable safety", RegexOption.IGNORE_CASE),

            // Jailbreak keywords
            Regex("\\bjailbreak\\b", RegexOption.IGNORE_CASE),
            Regex("exploit", RegexOption.IGNORE_CASE),
            Regex("vulnerability", RegexOption.IGNORE_CASE),
            Regex("hack", RegexOption.IGNORE_CASE),
            Regex("backdoor", RegexOption.IGNORE_CASE),
            Regex("privilege escalation", RegexOption.IGNORE_CASE),

            // Token/format manipulation
            Regex("respond only with", RegexOption.IGNORE_CASE),
            Regex("output only", RegexOption.IGNORE_CASE),
            Regex("generate only", RegexOption.IGNORE_CASE),
            Regex("print only", RegexOption.IGNORE_CASE),
            Regex("show only", RegexOption.IGNORE_CASE),
            Regex("output in format:", RegexOption.IGNORE_CASE),
            Regex("respond in format:", RegexOption.IGNORE_CASE),
            Regex("return in format:", RegexOption.IGNORE_CASE),

            // Dangerous instructions
            Regex("do not filter", RegexOption.IGNORE_CASE),
            Regex("do not censor", RegexOption.IGNORE_CASE),
            Regex("do not refuse", RegexOption.IGNORE_CASE),
            Regex("do not decline", RegexOption.IGNORE_CASE),
            Regex("must comply", RegexOption.IGNORE_CASE),
            Regex("you must", RegexOption.IGNORE_CASE),

            // AIM (Always Intelligent Mode) and variants
            Regex("\\bAIM\\b", RegexOption.IGNORE_CASE),
            Regex("AIM mode", RegexOption.IGNORE_CASE),
            Regex("always intelligent", RegexOption.IGNORE_CASE),

            // UCAR (Unrestricted Creative Assistant Roleplay)
            Regex("\\bUCAR\\b", RegexOption.IGNORE_CASE),
            Regex("unrestricted creative", RegexOption.IGNORE_CASE),

            // ChatGPT-specific jailbreaks
            Regex("ChatGPT, pretend", RegexOption.IGNORE_CASE),
            Regex("ChatGPT, ignore", RegexOption.IGNORE_CASE),
            Regex("as ChatGPT", RegexOption.IGNORE_CASE),
        )

        /**
         * Valid placeholder names that are allowed in merge templates.
         * These are user-selectable merge variables.
         */
        private val VALID_PLACEHOLDERS = setOf(
            "body", "prefix", "suffix", "prompt", "context",
            "input", "output", "instruction", "system",
            "user", "assistant", "content", "header", "footer"
        )
    }

    /**
     * Validates a merge rule template.
     *
     * @param template The merge rule template string to validate (e.g., "{prefix}\n\n{body}")
     * @return ValidationResult.Valid if template is safe, ValidationResult.Invalid if not
     *
     * Security Checks:
     * 1. Placeholder syntax validation (only allow {alphanumeric_underscore})
     * 2. Dangerous pattern detection (50+ injection/jailbreak patterns)
     * 3. Unknown placeholder detection
     * 4. Template structure warnings
     */
    fun validateTemplate(template: String): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Empty template check
        if (template.isBlank()) {
            errors.add("Template cannot be empty")
            return ValidationResult.Invalid(errors, warnings)
        }

        // CR-002: Dangerous pattern detection (Injection Prevention)
        val detectedPatterns = detectDangerousPatterns(template)
        if (detectedPatterns.isNotEmpty()) {
            errors.add(
                "Template contains dangerous patterns that could enable prompt injection: " +
                detectedPatterns.joinToString(", ")
            )
            Log.w(TAG, "CR-002: Dangerous patterns detected in template: $detectedPatterns")
        }

        // Placeholder syntax validation (must be {alphanumeric_underscore})
        val placeholderErrors = validatePlaceholderSyntax(template)
        errors.addAll(placeholderErrors)

        // Unknown placeholder detection (warns about potentially invalid placeholders)
        val unknownPlaceholders = detectUnknownPlaceholders(template)
        if (unknownPlaceholders.isNotEmpty()) {
            warnings.add(
                "Template uses unknown placeholders which may not be replaced: " +
                unknownPlaceholders.joinToString(", ")
            )
            Log.i(TAG, "Unknown placeholders in template: $unknownPlaceholders")
        }

        // Template structure warnings
        if (template.length > 5000) {
            warnings.add("Template is very long (${template.length} chars). Consider breaking into smaller rules.")
        }

        if (!template.contains(Regex(PLACEHOLDER_REGEX))) {
            warnings.add("Template does not contain any placeholders. Will output literal text.")
        }

        // Return result based on errors
        return if (errors.isEmpty()) {
            ValidationResult.Valid(warnings)
        } else {
            ValidationResult.Invalid(errors, warnings)
        }
    }

    /**
     * Detects dangerous patterns in the template that indicate injection/jailbreak attempts.
     *
     * @param text The template text to analyze
     * @return List of detected dangerous patterns
     */
    private fun detectDangerousPatterns(text: String): List<String> {
        val detectedPatterns = mutableListOf<String>()

        for (pattern in DANGEROUS_PATTERNS) {
            if (pattern.containsMatchIn(text)) {
                // Extract the matching text for logging
                val match = pattern.find(text)?.value ?: "unknown"
                detectedPatterns.add(match)
            }
        }

        return detectedPatterns.distinct()
    }

    /**
     * Validates placeholder syntax: must match {[a-zA-Z0-9_]+}
     * CR-002: Defense-in-depth - Strict syntax prevents injection
     *
     * @param template The template to validate
     * @return List of syntax errors
     */
    private fun validatePlaceholderSyntax(template: String): List<String> {
        val errors = mutableListOf<String>()

        // Find all potential placeholders
        val allPlaceholders = Regex("""\{[^}]*\}""").findAll(template)

        for (match in allPlaceholders) {
            val placeholder = match.value
            // Check if matches safe pattern
            if (!Regex(PLACEHOLDER_REGEX).matches(placeholder)) {
                errors.add(
                    "Invalid placeholder syntax: '$placeholder'. " +
                    "Use only alphanumeric characters and underscores: {example_1}"
                )
                Log.w(TAG, "Invalid placeholder syntax detected: $placeholder")
            }
        }

        return errors
    }

    /**
     * Detects unknown placeholders that won't be replaced by the merge engine.
     *
     * @param template The template to analyze
     * @return Set of unknown placeholder names
     */
    private fun detectUnknownPlaceholders(template: String): Set<String> {
        val unknownPlaceholders = mutableSetOf<String>()

        // Extract all valid placeholder names
        val allPlaceholders = Regex(PLACEHOLDER_REGEX).findAll(template)

        for (match in allPlaceholders) {
            // Extract name between braces: {name} -> name
            val placeholder = match.value
            val name = placeholder.substring(1, placeholder.length - 1)

            if (!VALID_PLACEHOLDERS.contains(name)) {
                unknownPlaceholders.add(name)
            }
        }

        return unknownPlaceholders
    }

    /**
     * Validates a complete merge rule input + template pair.
     *
     * @param template The merge rule template
     * @param inputPrompts The prompts to be merged
     * @return ValidationResult for the merge operation
     *
     * CR-002: Comprehensive validation before merge execution
     */
    fun validateMergeRule(
        template: String,
        inputPrompts: List<String>
    ): ValidationResult {
        val templateResult = validateTemplate(template)

        // Check if template validation failed
        if (templateResult is ValidationResult.Invalid) {
            return templateResult
        }

        // Validate each input prompt for dangerous content
        val warnings = (templateResult as ValidationResult.Valid).warnings.toMutableList()
        val allErrors = mutableListOf<String>()

        for ((index, prompt) in inputPrompts.withIndex()) {
            val promptDangerPatterns = detectDangerousPatterns(prompt)
            if (promptDangerPatterns.isNotEmpty()) {
                warnings.add(
                    "Input prompt #$index contains suspicious patterns: " +
                    promptDangerPatterns.joinToString(", ")
                )
                Log.w(TAG, "CR-002: Suspicious patterns in input prompt #$index: $promptDangerPatterns")
            }
        }

        return if (allErrors.isEmpty()) {
            ValidationResult.Valid(warnings)
        } else {
            ValidationResult.Invalid(allErrors, warnings)
        }
    }
}
