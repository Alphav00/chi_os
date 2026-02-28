# PromptVault Android: Complete Architecture Specification

## Executive Summary
PromptVault is a local-first Android application for managing, merging, and deploying AI prompts. The architecture prioritizes data sovereignty, performance at scale (1000+ prompts), and seamless prompt experimentation through merge-and-test workflows.

---

## 1. System Architecture Overview

### 1.1 Layered Architecture
```
┌─────────────────────────────────────────┐
│         Presentation Layer (Compose)    │
│    ├─ GalleryScreen (MVVM)             │
│    ├─ DetailScreen                      │
│    ├─ MergeScreen                       │
│    ├─ SettingsScreen                    │
│    └─ QuickLinksScreen                  │
├─────────────────────────────────────────┤
│         ViewModel Layer (Hilt)          │
│    ├─ PromptGalleryViewModel           │
│    ├─ MergeViewModel                    │
│    ├─ QuickLinkViewModel                │
│    └─ SettingsViewModel                 │
├─────────────────────────────────────────┤
│        Repository Layer (Data)          │
│    ├─ PromptRepository (Room)          │
│    ├─ MergeRuleRepository              │
│    ├─ QuickLinkRepository              │
│    └─ StorageManager (SAF/Cloud)       │
├─────────────────────────────────────────┤
│         Domain Layer (Business Logic)   │
│    ├─ AutoMergerEngine                 │
│    ├─ PromptSanitizer                  │
│    ├─ DeepLinkHandler                  │
│    └─ AnalyticsEngine                  │
├─────────────────────────────────────────┤
│    Database Layer (Room + SQLite)      │
│    ├─ PromptEntity                     │
│    ├─ MergeSessionEntity               │
│    ├─ QuickLinkEntity                  │
│    └─ UsageStatEntity                  │
└─────────────────────────────────────────┘
```

### 1.2 Core Dependencies
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Repository + Domain Layer
- **DI Container**: Hilt (annotation-based DI)
- **Database**: Room (SQLite with type-safe queries)
- **Async**: Kotlin Coroutines + Flow/StateFlow
- **Pagination**: Paging 3 (for galleries >500 prompts)
- **Storage I/O**: Storage Access Framework (SAF) + MediaStore
- **Networking Stub**: Retrofit + OkHttp (for future cloud sync)
- **Testing**: JUnit4, MockK, Compose Test Framework

---

## 2. Data Model Design

### 2.1 Entity Relationship Diagram
```
┌──────────────┐         ┌──────────────┐
│   Prompt     │◄────────│  Tag         │
│ (PK: id)     │ (M:M)   │ (PK: tagId)  │
├──────────────┤         ├──────────────┤
│ id           │         │ tagId        │
│ title        │         │ name         │
│ content      │         │ colorHex     │
│ createdAt    │         │ promptCount  │
│ updatedAt    │         └──────────────┘
│ favorite     │
│ usageCount   │         ┌──────────────┐
│ lastUsed     │◄────────│ Collection   │
│ mergeHistory │ (1:M)   │ (PK: colId)  │
└──────────────┘         ├──────────────┤
       │                 │ colId        │
       │                 │ name         │
       │                 │ description  │
       │                 │ promptIds[]  │
       │                 │ createdAt    │
       │                 └──────────────┘
       │
       ├─────────┐
       │         │
┌──────▼──────────▼──┐
│  MergeSession      │
│  (PK: sessionId)   │
├────────────────────┤
│ sessionId          │
│ ruleId             │
│ inputPromptIds[]   │
│ outputPromptId     │
│ confidenceScore    │
│ createdAt          │
└────────────────────┘
       │
       │
┌──────▼──────────────┐
│  MergeRule         │
│  (PK: ruleId)      │
├────────────────────┤
│ ruleId             │
│ name               │
│ template           │
│ variables (JSON)   │
│ isGlobal           │
│ usageCount         │
└────────────────────┘

┌──────────────────┐
│  QuickLink       │
│  (PK: linkId)    │
├──────────────────┤
│ linkId           │
│ label            │
│ deepLinkUri      │
│ targetPromptId   │
│ targetColId      │
│ sortOrder        │
│ createdAt        │
└──────────────────┘

┌──────────────────┐
│  UsageStat       │
│  (PK: statId)    │
├──────────────────┤
│ statId           │
│ promptId (FK)    │
│ date             │
│ actionType       │
│ metadata (JSON)  │
└──────────────────┘
```

### 2.2 Kotlin Data Classes

```kotlin
// Domain Models
@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val favorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val usageCount: Int = 0,
    val lastUsed: Instant? = null,
    val mergeHistory: List<MergeRecord> = emptyList(),
    val sourceLanguage: String = "en",
    val targetAudience: String = "general",
    val complexity: ComplexityLevel = ComplexityLevel.INTERMEDIATE
)

enum class ComplexityLevel { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }

data class MergeRecord(
    val sessionId: String,
    val ruleName: String,
    val timestamp: Instant,
    val confidenceScore: Float
)

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true) val colId: Long = 0,
    val name: String,
    val description: String = "",
    val promptIds: List<Long> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val color: String = "#FF6200EE"
)

@Entity(tableName = "merge_rules")
data class MergeRule(
    @PrimaryKey(autoGenerate = true) val ruleId: Long = 0,
    val name: String,
    val description: String = "",
    val template: String, // "{prefix}{body}{suffix}"
    val variables: String = "{}", // JSON: { "prefix": "Consider: ", ... }
    val isGlobal: Boolean = false,
    val usageCount: Int = 0,
    val category: String = "custom"
)

@Entity(tableName = "quick_links")
data class QuickLink(
    @PrimaryKey(autoGenerate = true) val linkId: Long = 0,
    val label: String,
    val deepLinkUri: String, // "promptvault://v2/prompt/{id}"
    val targetPromptId: Long? = null,
    val targetColId: Long? = null,
    val iconResId: String = "ic_prompt",
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now()
)

@Entity(tableName = "usage_stats")
data class UsageStat(
    @PrimaryKey(autoGenerate = true) val statId: Long = 0,
    val promptId: Long,
    val date: LocalDate = LocalDate.now(),
    val actionType: String, // "view", "copy", "export", "merge"
    val metadata: String = "{}" // JSON for extra context
)
```

---

## 3. Component Responsibilities

### 3.1 Repository Layer
```
PromptRepository
├─ suspend fun getAllPrompts(): Flow<List<Prompt>>
├─ suspend fun getPromptById(id: Long): Prompt?
├─ suspend fun searchPrompts(query: String): Flow<List<Prompt>>
├─ suspend fun insertPrompt(prompt: Prompt): Long
├─ suspend fun updatePrompt(prompt: Prompt)
├─ suspend fun deletePrompt(promptId: Long)
├─ suspend fun incrementUsage(promptId: Long)
├─ suspend fun addTag(promptId: Long, tag: String)
└─ suspend fun exportToJson(): ByteArray

MergeRuleRepository
├─ suspend fun getAllRules(): Flow<List<MergeRule>>
├─ suspend fun getRuleById(id: Long): MergeRule?
├─ suspend fun saveRule(rule: MergeRule): Long
├─ suspend fun deleteRule(ruleId: Long)
└─ suspend fun getGlobalRules(): Flow<List<MergeRule>>

QuickLinkRepository
├─ suspend fun getAllLinks(): Flow<List<QuickLink>>
├─ suspend fun createLink(link: QuickLink): Long
├─ suspend fun updateLink(link: QuickLink)
└─ suspend fun deleteLink(linkId: Long)
```

### 3.2 Domain/Business Logic Layer
```
AutoMergerEngine
├─ suspend fun validateTemplate(template: String): ValidationResult
├─ suspend fun preview(request: MergeRequest): String
├─ suspend fun execute(request: MergeRequest): Result<MergeSession>
└─ suspend fun scoreConfidence(inputs: List<Prompt>): Float

PromptSanitizer
├─ fun sanitizeInput(text: String): String
├─ fun detectInjectionRisk(text: String): Boolean
├─ fun flagHighRiskPatterns(text: String): List<RiskFlag>
└─ fun applyContentPolicy(text: String): String

DeepLinkHandler
├─ fun buildDeepLink(promptId: Long): String
├─ fun resolveDeepLink(uri: Uri): NavigationCommand
├─ fun validateLink(uri: String): Boolean
└─ fun migrateOldFormat(oldUri: String): String

AnalyticsEngine
├─ suspend fun recordAction(promptId: Long, action: String)
├─ suspend fun getUsageStats(promptId: Long): List<UsageStat>
├─ suspend fun getTrendingPrompts(): List<Prompt>
└─ suspend fun exportAnalytics(): String

StorageManager
├─ suspend fun exportToCloud(prompt: Prompt): Boolean
├─ suspend fun importFromCloud(cloudId: String): Prompt?
├─ suspend fun backupAll(): File
├─ suspend fun restoreFromBackup(backupFile: File): Boolean
└─ fun schedulePeriodicBackup(): WorkRequest
```

### 3.3 ViewModel Layer (MVVM)
```
PromptGalleryViewModel(
    promptRepo: PromptRepository,
    analyticsEngine: AnalyticsEngine
)
├─ uiState: StateFlow<GalleryUiState>
├─ fun loadPrompts()
├─ fun searchPrompts(query: String)
├─ fun toggleFavorite(promptId: Long)
├─ fun deletePrompt(promptId: Long)
└─ fun filterByTag(tag: String)

MergeViewModel(
    mergeRepo: MergeRuleRepository,
    mergerEngine: AutoMergerEngine
)
├─ mergeState: StateFlow<MergeState>
├─ fun loadRules()
├─ fun previewMerge(rule: MergeRule, prompts: List<Prompt>)
├─ fun executeMerge()
└─ fun saveAsNewPrompt()

SettingsViewModel(
    settingsDataStore: DataStore<Settings>
)
├─ settings: StateFlow<Settings>
├─ fun updateTheme(theme: Theme)
├─ fun enableBackupReminder(enabled: Boolean)
└─ fun setMergeStrategy(strategy: MergeStrategy)
```

---

## 4. UI/Presentation Layer

### 4.1 Screen Hierarchy
```
PromptVaultApp
├─ BottomNavigation (4 tabs)
│  ├─ Tab 1: GalleryScreen
│  ├─ Tab 2: MergeScreen
│  ├─ Tab 3: CollectionsScreen
│  └─ Tab 4: SettingsScreen
├─ FAB: CreatePromptScreen (ModalBottomSheet)
└─ AppBar: Search + Filter buttons
```

### 4.2 Key Screens
**GalleryScreen**: Lazy grid of prompt cards with:
- Card: Title, preview (first 100 chars), favorite toggle, last-used badge
- Filter chips: Tags, complexity, date range
- SearchBar: Full-text search across title + content
- Action buttons: Copy to clipboard, share, merge, delete

**MergeScreen**: Dual-pane interface:
- Left: Merge rule selector (dropdown + template preview)
- Center: Input prompt selector (multi-select)
- Right: Confidence score + Preview
- Actions: Test merge → Confirm → Save as new prompt

**DetailScreen**: Full prompt view:
- Title + Content (editable)
- Metadata: Created, updated, usage count, complexity
- Merge history: Timeline of merge operations
- Export: Copy, Share, Export as MD/JSON
- Related: Recommended merge rules

**SettingsScreen**:
- Theme: Light/Dark/Auto
- Backup: Manual backup, auto-backup frequency, cloud sync settings
- Merge defaults: Default merge strategy, confidence threshold
- Storage: Database size, cache clearing, export location

---

## 5. State Management Strategy

### 5.1 Data Flow (Unidirectional)
```
User Action
    ↓
ViewModel.event()
    ↓
Repository.operation()
    ↓
Room Database / External Storage
    ↓
Flow<T> emitted
    ↓
ViewModel collects → uiState.update()
    ↓
UI recomposes (Compose automatic)
```

### 5.2 Error Handling
```
Result<T> sealed class
├─ Success(data: T)
├─ Error(exception: Exception, userMessage: String)
└─ Loading

UI Snackbar triggered on Result.Error
Navigation handled via NavigationEvent sealed class
```

---

## 6. Caching Strategy

### 6.1 In-Memory Cache (LRU)
```kotlin
val promptCache = LruCache<Long, Prompt>(maxSize = 100)
val mergeRuleCache = LruCache<Long, MergeRule>(maxSize = 50)
```
- Cache invalidated on Update/Delete operations
- Accessed before Room database queries
- Reduces database I/O for frequently accessed prompts

### 6.2 Database Caching
- Room entities cached by SQLite query planner
- No explicit pagination except for lazy loading (Paging 3)
- Search results limited to 10 results at a time (infinite scroll)

---

## 7. Performance Targets

| Metric | Target | Status |
|--------|--------|--------|
| App startup (cold) | <2000ms | Via lazy initialization of Room |
| Gallery load (100 prompts) | <500ms | Pagination + Paging 3 |
| Merge execution | <1000ms | Coroutine Dispatchers.Default |
| Search latency | <300ms | Full-text search index on Room |
| Memory footprint | <100MB | LRU cache limits + Flow backpressure |

---

## 8. Security Considerations

### 8.1 Data Protection
- **Prompt encryption at rest**: Optional (Device Credential integration)
- **Export protection**: Show warning dialog before exporting to unsafe locations
- **Injection mitigation**: PromptSanitizer.sanitizeInput() removes risky patterns
- **Biometric auth**: Optional unlock for sensitive operations (export, delete all)

### 8.2 Content Policy
- Automatic flagging of prompts matching "jailbreak" patterns
- User warnings before merging risky prompts
- No telemetry by default; opt-in analytics only

---

## 9. Testing Strategy

### 9.1 Unit Tests
```
PromptRepositoryTest
├─ testInsertPrompt()
├─ testSearchPrompts()
└─ testIncrementUsage()

AutoMergerEngineTest
├─ testValidateTemplate()
├─ testSanitizeInput()
└─ testConfidenceScoring()

MergeRuleValidatorTest
├─ testValidPlaceholders()
├─ testInvalidTemplate()
└─ testDangerousPatterns()
```

### 9.2 UI Tests (Compose)
```
GalleryScreenTest
├─ testPromptCardsDisplay()
├─ testSearchFunctionality()
└─ testFavoriteToggle()

MergeScreenTest
├─ testMergePreview()
├─ testConfidenceDisplay()
└─ testSaveAsNewPrompt()
```

### 9.3 Integration Tests
- Database + Repository workflows
- Deep link resolution + navigation
- File import/export round-trip

---

## 10. Deployment Architecture

### 10.1 Build Variants
```
debugBuildType
├─ BuildConfig: API logging, crash reporter disabled
└─ Signing: Debug keystore

releaseBuildType
├─ BuildConfig: Optimized, Crashlytics enabled
├─ Proguard: Enabled
├─ Signing: Play Store keystore
└─ Min API: 24
```

### 10.2 Distribution
- Internal testing → Closed testing → Open beta → Production
- Firebase App Distribution for pre-release
- Google Play Console gradual rollout (10% → 50% → 100%)

---

## 11. Future Extensions

### 11.1 Cloud Sync (Phase 2)
- Firebase Firestore: Encrypted prompt storage
- Conflict resolution: Last-write-wins or user merge
- Offline-first with WorkManager sync queue

### 11.2 AI Integration (Phase 3)
- In-app prompt execution via OpenAI/Claude API
- Real-time merge suggestions based on LLM scoring
- A/B testing framework for prompt variants

### 11.3 Collaboration (Phase 4)
- Share prompt collections via unique links
- Comment threads on prompts
- Version history with rollback

---

## 12. Documentation Index

| Document | Purpose |
|----------|---------|
| ARCHITECTURE.md (this file) | System design + component contracts |
| API_SPEC.md | REST/Room DAO signatures |
| TESTING_PLAN.md | Test coverage + CI/CD pipeline |
| DEPLOYMENT.md | Release process + rollback procedures |
| USER_GUIDE.md | Feature walkthroughs + FAQs |

---

**Document Version**: 1.0
**Last Updated**: 2026-02-27
**Owner**: Platform Team
