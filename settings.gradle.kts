// ============ Gradle Settings for PromptVault Android ============
// DEVOPS: Refer to DEVOPS_AND_TEAMS.md Part 2 for CI/CD configuration

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "PromptVault"
include(":app")
