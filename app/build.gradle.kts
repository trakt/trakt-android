import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

private val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "tv.trakt.trakt"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "tv.trakt.trakt"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TRAKT_API_KEY", localProperties.getProperty("TRAKT_API_KEY"))
        buildConfigField("String", "TRAKT_API_SECRET", localProperties.getProperty("TRAKT_API_SECRET"))
    }

    signingConfigs {
        val keystoreFile = rootProject.file("keystore.jks")
        val keystorePassword: String = localProperties.getProperty("KEYSTORE_PASSWORD")
        val keystoreAlias: String = localProperties.getProperty("KEYSTORE_ALIAS")
        val keystoreKeyPassword: String = localProperties.getProperty("KEYSTORE_KEY_PASSWORD")

        create("release") {
            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = keystoreAlias
            keyPassword = keystoreKeyPassword
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    kotlin {
        jvmToolchain(11)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":tv"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)

    // Android Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.tooling.preview)
    debugImplementation(libs.androidx.compose.tooling)

    // Testing

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
