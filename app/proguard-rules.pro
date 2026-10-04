# Add project specific ProGuard rules here.
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-dontwarn javax.annotation.**

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models
-keep class com.example.data.model.** { *; }

# OkHttp & Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Android Architecture Components
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep Compose view models
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(...);
}
