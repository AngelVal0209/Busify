plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.busify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.busify"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    //svg
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // ZXing QR
    implementation("com.google.zxing:core:3.5.3")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.4")

    // Shimmer
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.1")

    // QR Scanner (Google Play Services)
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
