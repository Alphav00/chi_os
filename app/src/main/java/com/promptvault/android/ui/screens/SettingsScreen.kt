package com.promptvault.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.promptvault.android.ui.settings.SettingsViewModel
import com.promptvault.android.ui.settings.AppSettings
import com.promptvault.android.ui.settings.ThemeMode
import com.promptvault.android.ui.settings.ExportFormat
import com.promptvault.android.ui.settings.MergeStrategy

/**
 * SettingsScreen: User preferences and app configuration
 *
 * Responsibilities:
 * 1. Theme selector (Light/Dark/Auto)
 * 2. Backup settings (manual backup button, auto-backup frequency)
 * 3. Merge preferences (default rule, confidence threshold)
 * 4. Export format preferences (JSON/Markdown/CSV)
 * 5. Analytics opt-in toggle (local-only by default)
 * 6. Biometric auth toggle (optional)
 * 7. Storage usage display
 * 8. About section with version + changelog
 *
 * References:
 * - TIMELINE.md Milestone 3.3 (Settings)
 * - RISK_ASSESSMENT.md HR-005 (Privacy Trust)
 * - DEVOPS_AND_TEAMS.md: UI Engineer 2 responsibility
 *
 * Comments:
 * - HR-005: Privacy-first - Analytics disabled by default, opt-in model
 */

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val storageUsageMB by viewModel.storageUsageMB.collectAsStateWithLifecycle(0f)

    var showExportFormatDropdown by remember { mutableStateOf(false) }
    var showMergeStrategyDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }
    var showBackupFrequencyDropdown by remember { mutableStateOf(false) }

    // Load settings on first composition
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // SECTION 1: THEME SETTINGS
            item {
                SettingsSectionHeader("Appearance")
            }

            item {
                SettingRow(
                    title = "Theme",
                    subtitle = "Light, Dark, or Auto",
                    value = settings.themeMode.displayName,
                    onClick = { showThemeDropdown = !showThemeDropdown }
                )

                if (showThemeDropdown) {
                    ThemeDropdown(
                        selectedTheme = settings.themeMode,
                        onThemeSelected = { theme ->
                            viewModel.updateTheme(theme)
                            showThemeDropdown = false
                        }
                    )
                }
            }

            // SECTION 2: BACKUP SETTINGS
            item {
                SettingsSectionHeader("Backup & Storage")
            }

            item {
                Button(
                    onClick = { viewModel.createManualBackup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create Manual Backup Now")
                }
            }

            item {
                SettingToggle(
                    title = "Auto-Backup (Nightly)",
                    subtitle = "Automatically backup prompts every night",
                    checked = settings.autoBackupEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setAutoBackupEnabled(enabled)
                    }
                )
            }

            item {
                SettingRow(
                    title = "Auto-Backup Frequency",
                    subtitle = "How often to backup",
                    value = settings.backupFrequency,
                    onClick = { showBackupFrequencyDropdown = !showBackupFrequencyDropdown },
                    enabled = settings.autoBackupEnabled
                )

                if (showBackupFrequencyDropdown && settings.autoBackupEnabled) {
                    BackupFrequencyDropdown(
                        selectedFrequency = settings.backupFrequency,
                        onFrequencySelected = { frequency ->
                            viewModel.setBackupFrequency(frequency)
                            showBackupFrequencyDropdown = false
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Storage: ${String.format("%.2f", storageUsageMB)} MB used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp)
                )
            }

            // SECTION 3: MERGE PREFERENCES
            item {
                SettingsSectionHeader("Merge Preferences")
            }

            item {
                SettingRow(
                    title = "Default Merge Strategy",
                    subtitle = "Template to use by default",
                    value = settings.defaultMergeStrategy.displayName,
                    onClick = { showMergeStrategyDropdown = !showMergeStrategyDropdown }
                )

                if (showMergeStrategyDropdown) {
                    MergeStrategyDropdown(
                        selectedStrategy = settings.defaultMergeStrategy,
                        onStrategySelected = { strategy ->
                            viewModel.setDefaultMergeStrategy(strategy)
                            showMergeStrategyDropdown = false
                        }
                    )
                }
            }

            item {
                SettingSlider(
                    title = "Confidence Threshold",
                    subtitle = "Minimum confidence to execute merge",
                    value = settings.confidenceThreshold,
                    range = 0f..1f,
                    onValueChange = { threshold ->
                        viewModel.setConfidenceThreshold(threshold)
                    }
                )
            }

            // SECTION 4: EXPORT SETTINGS
            item {
                SettingsSectionHeader("Export & Share")
            }

            item {
                SettingRow(
                    title = "Export Format",
                    subtitle = "Default format for exporting prompts",
                    value = settings.defaultExportFormat.displayName,
                    onClick = { showExportFormatDropdown = !showExportFormatDropdown }
                )

                if (showExportFormatDropdown) {
                    ExportFormatDropdown(
                        selectedFormat = settings.defaultExportFormat,
                        onFormatSelected = { format ->
                            viewModel.setExportFormat(format)
                            showExportFormatDropdown = false
                        }
                    )
                }
            }

            // SECTION 5: PRIVACY & SECURITY
            item {
                SettingsSectionHeader("Privacy & Security")
            }

            item {
                SettingToggle(
                    title = "Analytics (Local Only)",
                    subtitle = "Track usage stats (never shared, stored locally)",
                    checked = settings.analyticsEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setAnalyticsEnabled(enabled)
                    }
                )

                // HR-005: Privacy-first - Analytics disabled by default, opt-in model
                if (!settings.analyticsEnabled) {
                    Text(
                        text = "Analytics are disabled. Your privacy is protected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                    )
                }
            }

            item {
                SettingToggle(
                    title = "Biometric Authentication",
                    subtitle = "Use fingerprint or face ID to unlock app (optional)",
                    checked = settings.biometricAuthEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setBiometricAuthEnabled(enabled)
                    }
                )
            }

            // SECTION 6: ABOUT
            item {
                SettingsSectionHeader("About")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PromptVault",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Version ${settings.appVersion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Advanced prompt management and merging tool.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Made with attention to user control and privacy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Spacer at end
            item {
                Box(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onClick != null) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    subtitle: String? = null,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Text(
                    text = String.format("%.0f%%", value * 100),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            androidx.compose.material3.Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeDropdown(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            ThemeMode.values().forEach { theme ->
                DropdownItem(
                    label = theme.displayName,
                    isSelected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) }
                )
                if (theme != ThemeMode.values().last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun ExportFormatDropdown(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            ExportFormat.values().forEach { format ->
                DropdownItem(
                    label = format.displayName,
                    isSelected = selectedFormat == format,
                    onClick = { onFormatSelected(format) }
                )
                if (format != ExportFormat.values().last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun MergeStrategyDropdown(
    selectedStrategy: MergeStrategy,
    onStrategySelected: (MergeStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            MergeStrategy.values().forEach { strategy ->
                DropdownItem(
                    label = strategy.displayName,
                    isSelected = selectedStrategy == strategy,
                    onClick = { onStrategySelected(strategy) }
                )
                if (strategy != MergeStrategy.values().last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun BackupFrequencyDropdown(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencies = listOf("Daily", "Weekly", "Monthly")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            frequencies.forEach { frequency ->
                DropdownItem(
                    label = frequency,
                    isSelected = selectedFrequency == frequency,
                    onClick = { onFrequencySelected(frequency) }
                )
                if (frequency != frequencies.last()) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun DropdownItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
