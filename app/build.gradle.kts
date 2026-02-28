// App-level build.gradle.kts for PromptVault Android
// DEVOPS: Refer to DEVOPS_AND_TEAMS.md Part 2 for CI/CD configuration
// Version policy: https://semver.org/ - See TIMELINE_AND_MILESTONES.md for versioning

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jmailen.kotlinter")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.promptvault.android"
    compileSdk = 34

    // Signing configuration must be declared before buildTypes that reference it
    signingConfigs {
        create("release") {
            // DEVOPS: These are placeholders. Actual values injected via CI/CD environment variables
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore/release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "placeholder"
            keyAlias = System.getenv("KEY_ALIAS") ?: "placeholder"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "placeholder"
        }
    }

    defaultConfig {
        applicationId = "com.promptvault.android"
        minSdk = 24
        targetSdk = 34
        versionCode = System.getenv("BUILD_NUMBER")?.toInt() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0-SNAPSHOT"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            buildConfigField("boolean", "LOG_API_CALLS", "true")
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.promptvault.local/\"")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "LOG_API_CALLS", "false")
            buildConfigField("String", "API_BASE_URL", "\"https://api.promptvault.com/\"")
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/proguard/androidx-*.pro",
                "DebugProbesKt.bin"
            )
        }
    }

    lint {
        checkReleaseBuilds = true
        checkDependencies = true
        ignoreTestSources = true
        informational += "MissingTranslation"
    }
}

dependencies {
    // ============ Core Android ============
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.1")

    // ============ Jetpack Compose & Material 3 ============
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.ui:ui-graphics:1.6.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.4")
    implementation("androidx.compose.foundation:foundation:1.6.4")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.4")

    // ============ Navigation ============
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ============ Room Database ============
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ============ Hilt Dependency Injection ============
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    ksp("com.google.dagger:hilt-compiler:2.50")

    // ============ Kotlin Coroutines & Flow ============
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ============ Paging 3 ============
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")

    // ============ Data Store ============
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ============ Work Manager (Background Jobs) ============
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ============ Storage Access Framework ============
    implementation("androidx.documentfile:documentfile:1.0.1")

    // ============ Network (Stub for future cloud sync) ============
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // ============ Firebase ============
    implementation("com.google.firebase:firebase-bom:32.7.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ============ Security & Biometrics ============
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.1.0")

    // ============ Serialization ============
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20231013")

    // ============ Logging ============
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ============ Image Loading (Future Enhancement) ============
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ============ Testing ============
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
    androidTestImplementation("io.mockk:mockk-android:1.13.9")

    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.4")

    // ============ Memory Leak Detection (Debug Only) ============
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    // ============ Code Quality ============
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")
}

// Detekt configuration for static analysis
detekt {
    toolVersion = "1.23.5"
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/detekt.yml")
    basePath = rootProject.projectDir.absolutePath
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
    }
}

