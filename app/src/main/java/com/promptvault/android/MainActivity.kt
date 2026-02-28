package com.promptvault.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.promptvault.android.ui.theme.PromptVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * MainActivity - Entry Point for PromptVault Android Application
 *
 * Responsibilities:
 * - Initialize Compose UI
 * - Handle deep links from deep link intent filters
 * - Set up navigation root
 * - Manage app lifecycle
 *
 * Deep link format: promptvault://v2/prompt/{id}
 * See AndroidManifest.xml for deep link configuration
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("MainActivity created")

        // Handle deep links if present
        intent.data?.let { uri ->
            Timber.i("Deep link received: $uri")
            // TODO: Route to appropriate screen based on deep link
            // See DEVOPS_AND_TEAMS.md DeepLinkHandler for deep link routing logic
        }

        setContent {
            PromptVaultTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PromptVaultApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        Timber.i("New intent received")

        intent?.data?.let { uri ->
            Timber.i("Deep link received in onNewIntent: $uri")
            // TODO: Route to appropriate screen based on deep link
        }
    }
}

/**
 * Temporary placeholder composable for app initialization
 * This will be replaced with proper Navigation graph setup
 * See TIMELINE_AND_MILESTONES.md Milestone 1.4 for Navigation setup
 */
@Composable
fun PromptVaultApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PromptVault") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PromptVault Android",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Environment: ${BuildConfig.ENVIRONMENT}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PromptVaultAppPreview() {
    PromptVaultTheme {
        PromptVaultApp()
    }
}
