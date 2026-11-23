plugins {
    id("com.android.application") version "8.9.2"
    id("org.jetbrains.kotlin.android") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

val keyPath = providers.environmentVariable("TUTURIUM_KEYSTORE")
android {
    namespace = "id.tuturium.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "id.tuturium.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 10306
        versionName = "1.3.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Client IDs are public identifiers, not OAuth client secrets.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",
            "\"662121711161-ukckhlfgtelnb8h85i69alnh1v5kldtp.apps.googleusercontent.com\"")
    }
    signingConfigs {
        if (keyPath.isPresent) create("production") {
            storeFile = file(keyPath.get())
            storePassword = providers.environmentVariable("TUTURIUM_STORE_PASSWORD").get()
            keyAlias = "tuturium"
            keyPassword = storePassword
        }
    }
    buildTypes {
        release {
            // This personal release targets 64-bit ARM devices; the debug APK remains
            // universal for emulator coverage. OCR quality/models stay unchanged.
            ndk { abiFilters.add("arm64-v8a") }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("production")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging.resources.excludes += setOf("META-INF/DEPENDENCIES", "META-INF/{LICENSE,NOTICE}*")
}
kotlin { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.6.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("org.jsoup:jsoup:1.19.1")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.05.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
