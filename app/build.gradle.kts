plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

// Keystore credentials resolution order:
//   1. local.properties (developer machine, git-ignored)
//   2. Environment variables (CI / GitHub Actions Secrets)
//   3. Hardcoded fallbacks (only so the current debug pipeline keeps building)
//
// For a real public release: regenerate jannah.keystore with a strong password,
// move it out of the repo, and set KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD
// as GitHub Actions Secrets. The file jannah.keystore in git history contains
// the current weak password and cannot be un-leaked by editing this file.
val localProps = java.util.Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun credential(key: String): String =
    localProps.getProperty(key) ?: System.getenv(key) ?: ""

android {
    signingConfigs {
        create("release") {
            storeFile = file("../jannah.keystore")
            storePassword = credential("KEYSTORE_PASSWORD").ifEmpty { "jannah123" }
            keyAlias = credential("KEY_ALIAS").ifEmpty { "jannah" }
            keyPassword = credential("KEY_PASSWORD").ifEmpty { "jannah123" }
        }
    }

    namespace = "com.jannahos"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.jannahos"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.batoulapps.adhan:adhan:1.2.1")
    implementation("net.zetetic:sqlcipher-android:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}
