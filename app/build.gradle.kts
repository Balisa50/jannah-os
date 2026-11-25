plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseStoreFile = providers.gradleProperty("SIRAJ_RELEASE_STORE_FILE")
    .orElse(providers.environmentVariable("SIRAJ_RELEASE_STORE_FILE")).orNull
val releaseStorePassword = providers.gradleProperty("SIRAJ_RELEASE_STORE_PASSWORD")
    .orElse(providers.environmentVariable("SIRAJ_RELEASE_STORE_PASSWORD")).orNull
val releaseKeyAlias = providers.gradleProperty("SIRAJ_RELEASE_KEY_ALIAS")
    .orElse(providers.environmentVariable("SIRAJ_RELEASE_KEY_ALIAS")).orNull
val releaseKeyPassword = providers.gradleProperty("SIRAJ_RELEASE_KEY_PASSWORD")
    .orElse(providers.environmentVariable("SIRAJ_RELEASE_KEY_PASSWORD")).orNull
val hasReleaseSigning = listOf(
    releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!
            }
        }
    }

    namespace = "com.jannahos"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.jannah.ramadan"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
    }
    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures { compose = true }
    lint { abortOnError = false }
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
