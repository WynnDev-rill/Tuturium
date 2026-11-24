import java.net.URI
import java.security.MessageDigest

plugins {
    id("com.android.application") version "8.9.2"
    id("org.jetbrains.kotlin.android") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
}

val keyPath = providers.environmentVariable("TUTURIUM_KEYSTORE")
// Pinned runtime only; model weights are never bundled in the APK.
val sherpaAar = layout.buildDirectory.file("offline-runtime/sherpa-onnx-static-1.13.4.aar")
val fetchSherpa = tasks.register("fetchSherpaRuntime") {
    outputs.file(sherpaAar)
    doLast {
        val destination = sherpaAar.get().asFile
        destination.parentFile.mkdirs()
        val temporary = File(destination.parentFile, destination.name + ".tmp")
        try {
            URI("https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.4/sherpa-onnx-static-link-onnxruntime-1.13.4.aar")
                .toURL().openStream().use { input -> temporary.outputStream().use { input.copyTo(it) } }
            val sha = MessageDigest.getInstance("SHA-256")
                .digest(temporary.readBytes()).joinToString("") { "%02x".format(it.toInt() and 255) }
            check(sha == "dc5ac19a28dee3bffc5e5a5d50cb6afa977703fc4a7ee535a308506990fdd295")
            check(temporary.renameTo(destination))
        } finally { temporary.delete() }
    }
}

android {
    namespace = "id.tuturium.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "id.tuturium.app"
        resourceConfigurations += listOf("en", "id")
        minSdk = 26
        targetSdk = 35
        versionCode = 10314
        versionName = "1.3.14"
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
    packaging.resources.excludes += setOf("META-INF/DEPENDENCIES", "META-INF/{LICENSE,NOTICE}*", "META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    // PDFBox's supported PDF encryption uses conventional algorithms. The provider also
    // ships lookup tables for the unused Picnic signatures and SIKE key encapsulation.
    packaging.resources.excludes += setOf(
        "org/bouncycastle/pqc/crypto/picnic/lowmc.properties",
        "org/bouncycastle/pqc/crypto/sike/*.properties"
    )
    packaging.jniLibs.pickFirsts += setOf("**/libc++_shared.so")
}
kotlin { compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
dependencies {
    implementation(files(sherpaAar).builtBy(fetchSherpa))
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("org.apache.commons:commons-compress:1.27.1")
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
