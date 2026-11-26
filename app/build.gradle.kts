import java.util.zip.GZIPOutputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sourceAssets = layout.projectDirectory.dir("src/main/assets")
val compressedHadithAssets = layout.buildDirectory.dir("generated/siraj-assets")
val compressHadithAsset by tasks.registering {
    inputs.dir(sourceAssets)
    outputs.dir(compressedHadithAssets)
    doLast {
        val sourceDirectory = sourceAssets.asFile
        val outputDirectory = compressedHadithAssets.get().asFile
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        sourceDirectory.walkTopDown()
            .filter { it.isFile && it.name != "hadiths_merged.json" }
            .forEach { sourceFile ->
                val target = outputDirectory.resolve(sourceFile.relativeTo(sourceDirectory).path)
                target.parentFile.mkdirs()
                sourceFile.copyTo(target, overwrite = true)
            }

        val sourceHadith = sourceDirectory.resolve("hadiths_merged.json")
        val compressedHadith = outputDirectory.resolve("hadiths_merged.json.gz")
        sourceHadith.inputStream().use { input ->
            GZIPOutputStream(compressedHadith.outputStream().buffered()).use { gzip ->
                input.copyTo(gzip)
            }
        }
    }
}

android {
    namespace = "com.jannahos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jannah.ramadan"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    sourceSets {
        getByName("main") {
            assets.setSrcDirs(listOf(compressedHadithAssets))
        }
    }

    buildTypes {
        release {
            // Release signing is intentionally not configured in source control.
            // Configure it locally or through protected CI secrets before distribution.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
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
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    testImplementation("junit:junit:4.13.2")
}


tasks.named("preBuild").configure {
    dependsOn(compressHadithAsset)
}
