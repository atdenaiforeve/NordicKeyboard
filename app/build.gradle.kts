plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.nordic.keyboard"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nordic.keyboard"
        minSdk = 26
        targetSdk = 35
        buildFeatures { buildConfig = true }
        versionCode = 2
        versionName = "0.1.1"
    }
    kotlin { jvmToolchain(17) }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
