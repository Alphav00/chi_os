package com.promptvault.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel: User preferences state management
 *
 * Responsibilities:
 * - Load and manage user settings (persisted to DataStore)
 * - Update theme, backup settings, merge preferences
 * - Manage export format and privacy settings
 * - Track storage usage
 * - Integrate with DataStore for persistence
 *
 * References:
 * - TIMELINE.md Milestone 3.3 (Settings & Preferences)
 * - ARCHITECTURE.md Section 5 (State Management)
 * - RISK_ASSESSMENT.md HR-005 (Privacy Trust)
 *
 * Comments:
 * - HR-005: Privacy-first - Analytics disabled by default, opt-in model
 */

enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AUTO("Auto")
}

enum class ExportFormat(val displayName: String) {
    JSON("JSON"),
    MARKDOWN("Markdown"),
    CSV("CSV")
}

enum class MergeStrategy(val displayName: String) {
    SIMPLE("Simple Concatenation"),
    TEMPLATE("Template-Based"),
    ADVANCED("Advanced Merge")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val autoBackupEnabled: Boolean = true,
    val backupFrequency: String = "Daily",
    val defaultMergeStrategy: MergeStrategy = MergeStrategy.TEMPLATE,
    val confidenceThreshold: Float = 0.5f,
    val defaultExportFormat: ExportFormat = ExportFormat.JSON,
    val analyticsEnabled: Boolean = false, // Privacy-first: Disabled by default
    val biometricAuthEnabled: Boolean = false,
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject DataStore<Preferences>
    // private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _storageUsageMB = MutableStateFlow(0f)
    val storageUsageMB: StateFlow<Float> = _storageUsageMB.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load settings from DataStore on app startup
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Load from dataStore
                // val flow: Flow<AppSettings> = dataStore.data.map { preferences ->
                //     AppSettings(
                //         themeMode = preferences[PreferencesKeys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.AUTO,
                //         autoBackupEnabled = preferences[PreferencesKeys.AUTO_BACKUP_ENABLED] ?: true,
                //         ...
                //     )
                // }
                // _settings.value = flow.first()

                // For now, use default settings
                _settings.value = AppSettings()
                calculateStorageUsage()
            } catch (e: Exception) {
                // Log error; keep default settings
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update theme preference and persist to DataStore
     */
    fun updateTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(themeMode = themeMode)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Enable/disable auto-backup and persist
     */
    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(autoBackupEnabled = enabled)

                if (enabled) {
                    // TODO: Schedule auto-backup task with WorkManager
                } else {
                    // TODO: Cancel scheduled auto-backup
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Set auto-backup frequency (Daily, Weekly, Monthly)
     */
    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(backupFrequency = frequency)

                if (_settings.value.autoBackupEnabled) {
                    // TODO: Reschedule auto-backup with new frequency
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Set default merge strategy
     */
    fun setDefaultMergeStrategy(strategy: MergeStrategy) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(defaultMergeStrategy = strategy)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Set confidence threshold for merge execution
     * Range: 0.0 (low) to 1.0 (high)
     */
    fun setConfidenceThreshold(threshold: Float) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                val clamped = threshold.coerceIn(0f, 1f)
                _settings.value = _settings.value.copy(confidenceThreshold = clamped)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Set default export format (JSON, Markdown, CSV)
     */
    fun setExportFormat(format: ExportFormat) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(defaultExportFormat = format)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Enable/disable analytics (local-only, never shared)
     * HR-005: Privacy-first - Analytics disabled by default, opt-in model
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(analyticsEnabled = enabled)

                if (enabled) {
                    // TODO: Start analytics tracking
                } else {
                    // TODO: Stop analytics tracking
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricAuthEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // TODO: Persist to dataStore
                _settings.value = _settings.value.copy(biometricAuthEnabled = enabled)

                if (enabled) {
                    // TODO: Set up biometric auth
                } else {
                    // TODO: Disable biometric auth
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Create a manual backup immediately
     * Called from "Create Manual Backup Now" button
     */
    fun createManualBackup() {
        viewModelScope.launch {
            try {
                // TODO: Trigger BackupManager.createManualBackup()
                // Shows success dialog when complete
            } catch (e: Exception) {
                // Show error dialog
            }
        }
    }

    /**
     * Calculate current device storage usage
     * Displays in Settings for user awareness
     */
    private fun calculateStorageUsage() {
        viewModelScope.launch {
            try {
                // TODO: Query database size + file system size
                val estimatedMB = 2.5f // Placeholder
                _storageUsageMB.value = estimatedMB
            } catch (e: Exception) {
                _storageUsageMB.value = 0f
            }
        }
    }
}
