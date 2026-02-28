# ============ ProGuard/R8 Configuration for PromptVault Android ============
# ProGuard rules for release builds
# Target: APK size <50MB (ARCHITECTURE section 7)
# DEVOPS: Review and update this file as new dependencies are added

# ============ General Android Configuration ============
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all public classes and their public members
-keep public class * {
    public protected *;
}

# ============ Hilt Dependency Injection ============
# Keep all Hilt-related annotations and classes
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep interface hilt_aggregated_deps.** { *; }

# Keep @HiltAndroidApp annotated classes
-keep @dagger.hilt.android.HiltAndroidApp class * {
    <init>();
}

# Keep @Module and @Provides annotated classes
-keep @dagger.Module class * {
    *;
}
-keep @dagger.Provides class * {
    *;
}

# Keep Hilt generated code
-keep class **_HiltModules { *; }
-keep class **_Factory { *; }
-keep class **_Impl { *; }
-keep class **_Component { *; }
-keep class **_Application { *; }

# ============ Room Database ============
# Keep Room entities and DAOs
-keep @androidx.room.Entity class * {
    <init>(...);
}

-keep @androidx.room.Dao class * {
    <methods>;
}

-keep @androidx.room.Database class * {
    *;
}

# Keep Room-generated classes
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep class **_Dao { *; }
-keep class **_Impl { *; }

# Keep TypeConverters for Room
-keep class * extends androidx.room.TypeConverter {
    <methods>;
}

# Keep data classes used as Room entities
-keep class com.promptvault.android.data.model.** {
    <init>(...);
    *;
}

# ============ Jetpack Compose ============
# Keep Compose internals and annotations
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.runtime.** { *; }

# Keep composable functions
-keep @androidx.compose.runtime.Composable class * {
    <methods>;
}

# Keep Compose theme and style classes
-keep class com.promptvault.android.ui.theme.** { *; }

# ============ Kotlin ============
# Keep Kotlin metadata and intrinsics
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep interface kotlinx.** { *; }

# Keep Kotlin reflection
-keepclassmembers class ** {
    ** CREATOR;
}

# Keep data classes with Kotlin metadata
-keepclassmembers class * {
    @kotlin.annotation.Retention *;
    @kotlin.annotation.Target *;
}

# ============ AndroidX ============
# Keep AndroidX lifecycle classes
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }

# Keep AndroidX navigation
-keep class androidx.navigation.** { *; }
-keep interface androidx.navigation.** { *; }

# Keep AndroidX DataStore
-keep class androidx.datastore.** { *; }
-keep interface androidx.datastore.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }
-keep interface androidx.work.** { *; }

# ============ Firebase ============
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Keep Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.crashlytics.** { *; }

# ============ Serialization (GSON & JSON) ============
# Keep GSON classes
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# Keep model classes used with GSON serialization
-keep class com.promptvault.android.data.model.** {
    *;
}

# Keep classes that have Gson annotations
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============ Retrofit & OkHttp ============
# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Retrofit service interfaces
-keep interface com.promptvault.android.data.network.** { *; }

# ============ Timber Logging ============
# Keep Timber classes for logging
-keep class timber.log.** { *; }
-keep class com.promptvault.android.util.** { *; }

# ============ Security ============
# Keep security and biometric classes
-keep class androidx.security.** { *; }
-keep class androidx.biometric.** { *; }

# ============ Application Code ============
# Keep all application classes and members (strict to preserve functionality)
-keep class com.promptvault.android.** {
    <init>(...);
    *;
}

# Keep public methods for deep linking
-keep class com.promptvault.android.MainActivity {
    <init>();
    public <methods>;
}

-keep class com.promptvault.android.PromptVaultApp {
    <init>();
    public <methods>;
}

# Keep ViewModels
-keep class com.promptvault.android.ui.** extends androidx.lifecycle.ViewModel {
    <init>(...);
    <methods>;
}

# Keep Repository classes
-keep class com.promptvault.android.data.repository.** {
    <init>(...);
    *;
}

# Keep Domain/Business Logic classes
-keep class com.promptvault.android.domain.** {
    <init>(...);
    *;
}

# ============ Optimization Settings ============
# Enable aggressive optimizations (safe with -keep rules above)
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ============ Debugging Settings ============
# Keep debug info for crash analysis
-keepattributes LocalVariableTable,LocalVariableTypeTable

# ============ Size Optimization ============
# Remove logging calls from release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ============ Suppress Warnings ============
# Safe to suppress warnings for external libraries with keep rules
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.checkerframework.**
