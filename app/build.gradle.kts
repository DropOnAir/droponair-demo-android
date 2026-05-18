plugins {
    id("com.android.application") version "8.4.2"
    id("org.jetbrains.kotlin.android") version "1.9.25"
}

android {
    namespace = "com.example.droponairdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.droponairdemo"
        minSdk  = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        // Replace with your values from panel.droponair.com
        buildConfigField("String", "DROPONAIR_APP_ID",         "\"YOUR_APP_ID\"")
        buildConfigField("String", "DROPONAIR_PUBLIC_API_KEY", "\"YOUR_PUBLIC_API_KEY\"")
        buildConfigField("String", "BACKEND_URL",              "\"http://10.0.2.2:8180\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding  = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // DropOnAir SDK (Maven Central)
    implementation("com.droponair:sdk-android:0.6.0")

    // AndroidX + Material
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // OkHttp for backend auth calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
