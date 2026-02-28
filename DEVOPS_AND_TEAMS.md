# PromptVault Android: DevOps Infrastructure & Team Structure

## Part 1: Organizational Structure

### Executive Leadership
```
Product Manager (Chief Product Officer)
├─ Android Tech Lead (VP Engineering)
├─ DevOps Lead (VP Infrastructure)
├─ QA Lead (Quality Assurance Director)
└─ Technical Writer (Documentation Lead)
```

### Engineering Org Chart
```
Android Tech Lead (1.0 FTE)
├─ Backend Engineer / Repository Lead (1.0 FTE)
│   ├─ Responsibility: Data layer, Room schemas, Repository pattern
│   ├─ Deliverables: DAO implementation, migration scripts
│   └─ Code Review: All data-layer PRs
│
├─ UI Engineer 1 (1.0 FTE)
│   ├─ Responsibility: Gallery, Detail, Collections screens
│   ├─ Deliverables: Compose UI, animations, responsive design
│   └─ Expertise: Jetpack Compose, Material Design 3
│
├─ UI Engineer 2 (1.0 FTE)
│   ├─ Responsibility: Merge, Settings, QuickLinks screens
│   ├─ Deliverables: Merge workflow, user preferences UI
│   └─ Expertise: Complex state management, deep linking
│
├─ Android System Integration Engineer (0.5 FTE shared)
│   ├─ Responsibility: Deep linking, widgets, system integration
│   ├─ Deliverables: DeepLinkHandler, launcher shortcuts
│   └─ Expertise: Android system APIs, broadcast receivers
│
├─ Performance Engineer (0.5 FTE shared)
│   ├─ Responsibility: Optimization, profiling, benchmarking
│   ├─ Deliverables: Startup time <2s, memory profiling
│   └─ Expertise: Android Profiler, Compose performance
│
└─ Database Engineer (0.5 FTE shared)
    ├─ Responsibility: Schema design, query optimization
    ├─ Deliverables: FTS index, migration testing
    └─ Expertise: SQLite, Room query optimization
```

### QA & Testing Organization
```
QA Lead (1.0 FTE)
├─ Test Automation Engineer (0.75 FTE)
│   ├─ Responsibility: Unit tests, integration tests, CI/CD
│   ├─ Tools: JUnit4, MockK, GitHub Actions
│   └─ Coverage Target: >85%
│
├─ UI Test Specialist (0.5 FTE)
│   ├─ Responsibility: Compose UI tests, screenshot tests
│   ├─ Tools: Compose Test Framework, Paparazzi
│   └─ Target: >60 UI test cases
│
└─ Device Lab Manager (0.25 FTE)
    ├─ Responsibility: Physical device testing, compatibility
    ├─ Tools: Firebase Test Lab, local device farm
    └─ Coverage: API 24-34, 5+ screen sizes
```

### DevOps & Infrastructure Organization
```
DevOps Lead (1.0 FTE)
├─ CI/CD Engineer (0.75 FTE)
│   ├─ Responsibility: GitHub Actions/Jenkins pipeline
│   ├─ Deliverables: Automated builds, test runs, deployments
│   └─ Tools: GitHub Actions, Gradle, Firebase App Distribution
│
├─ Release Engineer (0.5 FTE)
│   ├─ Responsibility: APK signing, versioning, Play Store
│   ├─ Deliverables: Release builds, rollout management
│   └─ Tools: Gradle signing, Play Console API
│
└─ Infra & Monitoring Engineer (0.25 FTE)
    ├─ Responsibility: Crash reporting, analytics
    ├─ Deliverables: Firebase Crashlytics setup, monitoring
    └─ Tools: Firebase, Datadog (optional)
```

### Other Functions
```
Security Engineer (0.5 FTE shared)
├─ Responsibility: Content policy, sanitization, data protection
├─ Deliverables: MergeRuleValidator, prompt sanitizer
└─ Review: Security-critical PRs

Technical Writer (0.25 FTE shared)
├─ Responsibility: User guide, API docs, onboarding
├─ Deliverables: README, API.md, user guide
└─ Tool: Markdown + screenshots

Product Manager (0.5 FTE shared)
├─ Responsibility: Requirements, launch strategy, roadmap
├─ Deliverables: Feature specs, launch checklist
└─ Stakeholder: User feedback collection
```

---

## Part 2: CI/CD Pipeline

### GitHub Actions Workflow

#### 1. Build Pipeline (`.github/workflows/build.yml`)
```yaml
name: Android Build
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        api-level: [24, 30, 34]

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Validate Gradle wrapper
        uses: gradle/wrapper-validation-action@v1

      - name: Run Gradle build
        uses: gradle/gradle-build-action@v2
        with:
          arguments: build

      - name: Upload build artifacts
        uses: actions/upload-artifact@v3
        with:
          name: apk-${{ matrix.api-level }}
          path: app/build/outputs/apk/
```

#### 2. Test Pipeline (`.github/workflows/test.yml`)
```yaml
name: Test
on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: ./gradlew testDebugUnitTest --no-daemon
      - uses: codecov/codecov-action@v3
        with:
          files: ./coverage/reports/

  ui-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: ./gradlew connectedAndroidTest --no-daemon
      - uses: actions/upload-artifact@v3
        with:
          name: ui-test-results
          path: app/build/reports/androidTests/
```

#### 3. Release Pipeline (`.github/workflows/release.yml`)
```yaml
name: Release
on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build release APK
        run: ./gradlew assembleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}

      - name: Sign APK
        run: |
          jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
            -keystore $KEYSTORE_FILE \
            -storepass $KEYSTORE_PASSWORD \
            -keypass $KEY_PASSWORD \
            app/build/outputs/apk/release/app-release-unsigned.apk $KEY_ALIAS

      - name: Upload to Google Play Console
        run: ./gradlew publishReleaseBundle
        env:
          PLAY_CONSOLE_KEY: ${{ secrets.PLAY_CONSOLE_KEY }}

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          files: app/build/outputs/apk/release/*.apk
```

#### 4. Code Quality Pipeline (`.github/workflows/lint.yml`)
```yaml
name: Lint & Code Quality
on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run ktlint
        run: ./gradlew ktlint

      - name: Run Android Lint
        run: ./gradlew lint

      - name: Run detekt (static analysis)
        run: ./gradlew detekt

      - name: Upload lint reports
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: lint-reports
          path: app/build/reports/
```

### Manual Deployment Process

#### Stage 1: Internal Testing (Week 1)
```bash
# Developer builds and deploys to internal testers
./gradlew bundleRelease
# Upload to Google Play Console → Internal testing track
# Collect feedback, report bugs to GitHub Issues
```

#### Stage 2: Closed Beta (Week 2)
```bash
# QA lead approves release candidate
# Upload to Google Play Console → Closed testing track
# Invite 50-100 beta testers
# Monitor Crashlytics for errors
```

#### Stage 3: Staged Production Rollout
```bash
# Monday: Roll out to 10% of users
# Monitor crash rate for 24 hours
# If crash rate <0.1%:
#   - Wednesday: Roll out to 50%
#   - Friday: Roll out to 100%
# If crash rate >0.1%:
#   - Halt rollout, investigate, fix, redeploy
```

---

## Part 3: Infrastructure & Hosting

### Development Environment

#### Local Setup
```bash
# Prerequisites
- Android Studio 2024.1+
- JDK 17
- Gradle 8.0+
- Android SDK 24-34

# Setup
git clone <repo>
cd PromptVault
./gradlew build
# Open in Android Studio → Sync gradle
# Run on emulator or device
```

#### Gradle Configuration (build.gradle.kts)
```kotlin
android {
    namespace = "com.promptvault.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.promptvault.android"
        minSdk = 24
        targetSdk = 34
        versionCode = System.getenv("BUILD_NUMBER")?.toInt() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0-SNAPSHOT"
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.release
        }
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // UI
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.material3:material3:1.2.0")

    // Data
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // DI
    implementation("com.google.dagger:hilt-android:2.50")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
}
```

### Firebase Integration

#### Crashlytics Setup
```kotlin
// In MainActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics

val crashlytics = FirebaseCrashlytics.getInstance()
crashlytics.setCrashlyticsCollectionEnabled(BuildConfig.DEBUG.not())

Thread.setDefaultUncaughtExceptionHandler { _, exception ->
    crashlytics.recordException(exception)
}
```

#### Analytics Setup (Local-Only by Default)
```kotlin
// In AnalyticsEngine.kt
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsEngine(private val analytics: FirebaseAnalytics) {
    suspend fun recordAction(promptId: Long, action: String) {
        // Local logging to Room database
        analyticsRepository.insert(UsageStat(
            promptId = promptId,
            actionType = action,
            timestamp = Instant.now()
        ))
        // Optional: Send to Firebase if user opts in
        if (preferencesDataStore.analyticsEnabled) {
            analytics.logEvent("prompt_action", bundleOf(
                "action_type" to action,
                "prompt_id" to promptId
            ))
        }
    }
}
```

### Google Play Console Configuration

#### App Listing
- **Title**: PromptVault - Manage & Merge AI Prompts
- **Description**:
  ```
  PromptVault helps you collect, organize, and merge AI prompts into powerful
  combinations. Perfect for prompt engineers, content creators, and anyone who
  works with language models.

  Features:
  - Create and organize unlimited prompts
  - Merge prompts using custom rules
  - Export to any AI chat interface
  - Quick access links and widgets
  - Local-first storage (your data stays on device)
  - Analytics and usage tracking
  ```
- **Category**: Productivity / Tools
- **Content Rating**: General Audiences

#### Release Management
- **Target Devices**: Phones (Android 7.0+)
- **Supported Languages**: English (extensible)
- **Pricing**: Free (with optional premium sync tier in Phase 2)
- **Release Schedule**: Weekly updates (if needed)

#### Distribution Tracks
| Track | Purpose | Audience | Duration |
|-------|---------|----------|----------|
| Internal testing | Developer QA | Team only | Ongoing |
| Closed testing | Beta feedback | 100 testers | 2 weeks |
| Open testing | Public beta | All Play Store users | 1-2 weeks |
| Production | Public release | All users | Ongoing |

---

## Part 4: Monitoring & Observability

### Metrics to Track

#### Performance Metrics
```
- App startup time (cold/warm)
- Gallery load time
- Search latency
- Merge execution time
- Memory usage (peak/average)
- Battery drain (mAh/hour)
- Frame rate (FPS)
```

#### Quality Metrics
```
- Crash rate (per mille)
- ANR (Application Not Responding) rate
- StrictMode violations
- Lint warnings
- Code coverage %
- Test pass rate %
```

#### User Metrics (Privacy-First)
```
- Daily Active Users (DAU)
- Monthly Active Users (MAU)
- Session length (average)
- Feature usage distribution
- Prompt gallery size (average)
- Merge rule creation rate
```

### Monitoring Tools

#### Firebase Crashlytics
```kotlin
// Automatic crash reporting
// Real-time notifications for critical crashes
// Historical crash data + stack traces
```

#### Android Studio Profiler
```
Weekly profiling runs:
- CPU Profiler: Identify hot paths
- Memory Profiler: Detect leaks
- Energy Profiler: Battery drain analysis
- Network Profiler: (for future cloud sync)
```

#### Custom Analytics Dashboard (Phase 2)
```
- Grafana/Datadog integration
- Daily metrics aggregation
- Automated alerting (e.g., crash spike)
- User cohort analysis
```

---

## Part 5: Security & Access Control

### Secret Management
```
Sensitive data stored in GitHub Secrets:
- KEYSTORE_PASSWORD: APK signing key password
- KEY_ALIAS: APK signing key alias
- KEY_PASSWORD: APK signing password
- PLAY_CONSOLE_KEY: Service account JSON
- FIREBASE_CONFIG: google-services.json

Access Control:
- Only DevOps team has secret management access
- All deployments logged and auditable
- Secrets rotated annually
```

### Code Access Control
```
GitHub CODEOWNERS:
- app/src/main/java/data/ → Backend Engineer
- app/src/main/java/ui/ → UI Engineers
- app/src/test/ → QA Engineer
- build.gradle.kts → DevOps Lead

PR Requirements:
- Minimum 2 approvals before merge
- All CI checks pass
- Code coverage maintained
- No security vulnerabilities
```

---

## Part 6: Disaster Recovery & Rollback

### Backup Strategy
```
Production APK Backups:
- Stored in GitHub Releases
- Stored in Google Play Console
- 90-day retention

Database Backups:
- User's local device (primary storage)
- Auto-backup to user's Drive (Google Photos integration)
- Cloud sync in Phase 2

Code Backups:
- GitHub repository (redundant)
- Signed git tags for each release
```

### Rollback Procedure
```
If production crash >1%:

1. Immediately halt Play Store rollout
2. Investigate crash logs in Crashlytics
3. Identify root cause and fix
4. Rebuild release APK
5. Test on Firebase Test Lab (API 24, 30, 34)
6. Redeploy with new version code (e.g., 1.0.1)
7. Resume staged rollout (10% → 50% → 100%)

Timeline: <2 hours for critical rollback
```

---

## Part 7: Documentation & Runbooks

### Key Documentation
```
/docs/
├─ SETUP.md - Local development setup
├─ ARCHITECTURE.md - System design
├─ API_SPEC.md - Data layer contracts
├─ TESTING.md - Test strategy
├─ DEPLOYMENT.md - Release process
├─ TROUBLESHOOTING.md - Common issues
└─ RUNBOOKS/
    ├─ emergency-rollback.md
    ├─ database-migration.md
    ├─ crash-investigation.md
    └─ performance-profiling.md
```

### Runbook: Emergency Rollback
```
1. Check Crashlytics dashboard
   - Identify crash rate and affected version

2. Revert to previous stable APK
   - Pull latest from GitHub Releases
   - Upload to Play Console (new version code)

3. Test rollback APK
   - Firebase Test Lab (10 minutes)
   - Internal testing track (30 minutes)

4. Deploy rollback
   - Push to closed testing
   - Wait 1 hour for feedback
   - If no new crashes: Push to 10% production

5. Root cause analysis
   - Investigate crash logs
   - Create GitHub issue + fix
   - Merge fix with 2 approvals

6. Re-release
   - Follow normal release pipeline
   - Start with internal → closed → production
```

---

**DevOps Document Version**: 1.0
**Last Updated**: 2026-02-27
**Owner**: DevOps & Infrastructure Team
