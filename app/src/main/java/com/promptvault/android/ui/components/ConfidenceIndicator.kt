package com.promptvault.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateAsState

/**
 * ConfidenceIndicator: Synesthesia-based confidence display
 *
 * XENOCOGNITIVE #3: Multi-sensory confidence feedback
 * - Visual: Color gradient (red 0% → yellow 50% → green 100%)
 * - Haptic: Vibration intensity (no vibration → strong buzz)
 * - Acoustic: Tone pitch (low C ~100Hz for 0% → high C ~1000Hz for 100%)
 * - Motion: Animated rotation and scale based on score
 *
 * PP-U3: Confidence anxiety mitigation
 * - Add social proof + undo option to reduce merge hesitation
 * - Display label indicates confidence level (SAFE/MODERATE/LOW)
 *
 * References:
 * - XENOCOGNITIVE_IDEAS.md #3 (Confidence Synesthesia)
 * - PRESSURE_POINTS.md PP-U3 (Confidence Anxiety)
 * - TIMELINE.md Milestone 2.4 (Merge UI Screen)
 */

data class ConfidenceSignal(
    val score: Float, // 0.0 to 1.0
    val vibrationType: VibrationType = VibrationType.NONE,
    val audioFrequency: Float = 100f, // Hz: 100 (low) to 1000 (high)
    val animationSpeed: Float = 800f, // Duration in ms: 1000 (slow) to 100 (fast)
    val colorGradient: Color = Color.Red
)

enum class VibrationType {
    NONE,
    GENTLE,
    STRONG,
    DOUBLE_TAP
}

@Composable
fun ConfidenceIndicator(
    score: Float,
    modifier: Modifier = Modifier,
    enableHaptics: Boolean = false,
    enableAudio: Boolean = false,
    showLabel: Boolean = true
) {
    // Clamp score to 0.0 - 1.0 range
    val clampedScore = score.coerceIn(0f, 1f)

    // Map confidence score to signal (color, haptic, audio, animation)
    val signal = remember(clampedScore) {
        mapConfidenceToSignal(clampedScore)
    }

    // Trigger haptic and audio feedback
    LaunchedEffect(signal) {
        if (enableHaptics) {
            // TODO: Integrate with HapticFeedbackManager
            // hapticFeedback.vibrate(signal.vibrationType)
        }
        if (enableAudio) {
            // TODO: Integrate with AudioPlayer
            // audioPlayer.playTone(frequency = signal.audioFrequency, duration = 500)
        }
    }

    // Animated rotation based on score (0-100%)
    val rotation by animateAsState(
        targetValue = clampedScore * 360f,
        label = "confidence-rotation"
    )

    // Animated scale (0.8 to 1.0)
    val scale by animateAsState(
        targetValue = 0.8f + clampedScore * 0.2f,
        label = "confidence-scale"
    )

    // Animated color
    val animatedColor by animateColorAsState(
        targetValue = signal.colorGradient,
        label = "confidence-color"
    )

    // Confidence label text
    val confidenceLabel = when {
        clampedScore >= 0.7f -> "SAFE"
        clampedScore >= 0.4f -> "MODERATE"
        else -> "LOW"
    }

    val confidenceDescription = when {
        clampedScore >= 0.7f -> "High confidence merge"
        clampedScore >= 0.4f -> "Moderate confidence merge"
        else -> "Low confidence merge - review carefully"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated gauge visualization
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(2.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Animated fill bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedScore)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(animatedColor)
                    .rotate(rotation * 0.1f) // Subtle rotation effect
            )

            // Percentage text in center
            Text(
                text = "${(clampedScore * 100).toInt()}%",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelSmall,
                color = if (clampedScore > 0.5f) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }

        // Confidence label and description
        if (showLabel) {
            Text(
                text = "Confidence: $confidenceLabel",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = animatedColor
            )

            Text(
                text = confidenceDescription,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Maps confidence score (0-1) to multi-sensory signal
 * Color gradient: Red (0%) → Yellow (50%) → Green (100%)
 * Haptic: None → Gentle → Strong intensity
 * Audio: 100Hz (low C) → 1000Hz (high C)
 * Animation: Slow (1000ms) → Fast (100ms)
 */
private fun mapConfidenceToSignal(score: Float): ConfidenceSignal {
    val color = interpolateColorGradient(score)

    val vibrationType = when {
        score < 0.2f -> VibrationType.NONE
        score < 0.4f -> VibrationType.GENTLE
        score < 0.7f -> VibrationType.STRONG
        else -> VibrationType.DOUBLE_TAP
    }

    // Audio frequency: 100Hz (low) to 1000Hz (high), nonlinear mapping
    val audioFrequency = 100f + (score * score * 900f)

    // Animation speed: 1000ms (slow) to 100ms (fast)
    val animationSpeed = 1000f - (score * 900f)

    return ConfidenceSignal(
        score = score,
        vibrationType = vibrationType,
        audioFrequency = audioFrequency,
        animationSpeed = animationSpeed,
        colorGradient = color
    )
}

/**
 * Color gradient interpolation: Red → Yellow → Green
 * 0.0 = Red (255, 0, 0)
 * 0.5 = Yellow (255, 255, 0)
 * 1.0 = Green (0, 255, 0)
 */
private fun interpolateColorGradient(score: Float): Color {
    return when {
        score < 0.5f -> {
            // Red to Yellow: increase green component
            val t = score * 2f // 0 to 1 over first half
            Color(
                red = 1f,
                green = t,
                blue = 0f,
                alpha = 1f
            )
        }
        else -> {
            // Yellow to Green: decrease red component
            val t = (score - 0.5f) * 2f // 0 to 1 over second half
            Color(
                red = 1f - t,
                green = 1f,
                blue = 0f,
                alpha = 1f
            )
        }
    }
}

/**
 * Simple confidence gauge without text labels (compact version)
 */
@Composable
fun ConfidenceGauge(
    score: Float,
    modifier: Modifier = Modifier
) {
    val clampedScore = score.coerceIn(0f, 1f)
    val signal = remember(clampedScore) { mapConfidenceToSignal(clampedScore) }

    val animatedColor by animateColorAsState(
        targetValue = signal.colorGradient,
        label = "gauge-color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.4f)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clampedScore)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(animatedColor)
        )
    }
}

/**
 * Confidence indicator with social proof context
 * PP-U3: Confidence anxiety mitigation - Add social proof + undo option
 * Shows similar merges that worked well and encourages merge execution
 */
@Composable
fun ConfidenceIndicatorWithSocialProof(
    score: Float,
    similarSuccessfulMerges: Int = 0,
    modifier: Modifier = Modifier,
    onExecute: (() -> Unit)? = null
) {
    val clampedScore = score.coerceIn(0f, 1f)
    val signal = remember(clampedScore) { mapConfidenceToSignal(clampedScore) }

    val recommendation = when {
        clampedScore >= 0.7f -> "Likely to work"
        clampedScore >= 0.4f -> "Moderate confidence"
        else -> "Review carefully"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main confidence gauge
        ConfidenceIndicator(
            score = score,
            showLabel = true,
            enableHaptics = false,
            enableAudio = false
        )

        // Social proof section
        if (similarSuccessfulMerges > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ Social Proof",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$similarSuccessfulMerges similar merges worked well",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // PP-U3: Undo reassurance
        Text(
            text = "You can always undo this merge",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
