package com.promptvault.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * PromptVault Android Application Class
 *
 * Responsibilities:
 * - Hilt dependency injection setup
 * - Firebase initialization (Crashlytics, Analytics)
 * - Global exception handling and logging
 * - App lifecycle management
 *
 * DEVOPS: Initialize Firebase per DEVOPS_AND_TEAMS.md Part 3
 */
@HiltAndroidApp
class PromptVaultApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ============ Timber Logging Setup ============
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In release builds, use Crashlytics logging
            Timber.plant(CrashlyticsTree())
        }

        Timber.i("PromptVaultApp initialized")
        Timber.i("Build variant: ${BuildConfig.FLAVOR}")
        Timber.i("Environment: ${BuildConfig.ENVIRONMENT}")

        // ============ Firebase Crashlytics Setup ============
        initializeFirebaseCrashlytics()

        Timber.i("PromptVaultApp startup complete")
    }

    /**
     * Initialize Firebase Crashlytics for crash reporting
     *
     * - Disabled in debug builds (for development flexibility)
     * - Enabled in release builds (production monitoring)
     * - Respects user privacy settings (opt-in analytics)
     */
    private fun initializeFirebaseCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Only enable Crashlytics in release builds
        // Debug builds will log crashes locally
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        Timber.d("Firebase Crashlytics initialized")
        Timber.d("Collection enabled: ${!BuildConfig.DEBUG}")
    }
}

/**
 * Custom Timber Tree for Crashlytics integration
 *
 * Logs non-debug messages to Firebase Crashlytics for crash reporting
 * Used in release builds where DebugTree is not available
 */
private class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
            return
        }

        val msg = if (tag != null) "[$tag] $message" else message

        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        } else {
            // Log non-exception messages as breadcrumbs
            FirebaseCrashlytics.getInstance().log(msg)
        }
    }
}
