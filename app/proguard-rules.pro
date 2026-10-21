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
