plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ru.repetitor.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.repetitor.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-trial"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
