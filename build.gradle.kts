repositories {
    google()
    mavenCentral()
}

plugins {
    id("com.android.application") version "8.5.0"
    kotlin("android") version "1.9.0"
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "hardware.xwtk.harpocrat.kbsync"
        minSdk = 30
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        // This enables privileged permissions (because you're in /vendor/priv-app)
        manifestPlaceholders["privileged"] = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

    namespace = "hardware.xwtk.harpocrat.kbsync"
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
}
