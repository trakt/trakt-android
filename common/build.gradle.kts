import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

private val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "tv.trakt.trakt.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        buildConfigField("String", "TRAKT_API_KEY", localProperties.getProperty("TRAKT_API_KEY"))
        buildConfigField("String", "TRAKT_API_SECRET", localProperties.getProperty("TRAKT_API_SECRET"))

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlin {
        jvmToolchain(11)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            java.srcDir("${rootDir}/build/generate-resources/main/src/main")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.datastore)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logger)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.negotiation)
    implementation(libs.ktor.client.auth)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android.compose)

    implementation(libs.kotlin.immutable.collections)
}
