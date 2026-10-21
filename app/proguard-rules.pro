# Keep SQLCipher native bindings
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**

# Keep Adhan library
-keep class com.batoulapps.adhan.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep App widget receivers
-keep class com.jannahos.NextPrayerWidget { *; }

# Tink / Security crypto missing annotations
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn javax.annotation.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
