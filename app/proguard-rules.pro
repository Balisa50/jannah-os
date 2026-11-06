# Keep Adhan library intact
-keep class com.batoulapps.adhan.** { *; }
-dontwarn com.batoulapps.adhan.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.** { *; }
