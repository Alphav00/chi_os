// ============ Top-level build file for PromptVault Android ============
// DEVOPS: Refer to DEVOPS_AND_TEAMS.md Part 2 for CI/CD configuration
// All plugin management and dependency resolution is in settings.gradle.kts

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("org.jmailen.kotlinter") version "4.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
