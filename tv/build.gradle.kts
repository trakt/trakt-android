import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

private val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "tv.trakt.trakt.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        buildConfigField("String", "TRAKT_API_KEY", localProperties.getProperty("TRAKT_API_KEY"))
        buildConfigField("String", "TRAKT_API_SECRET", localProperties.getProperty("TRAKT_API_SECRET"))

        buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }

    flavorDimensions += "version"
    productFlavors {
        create("playstore") {
            dimension = "version"
        }
        create("internal") {
            dimension = "version"
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":resources"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.app.review)
    implementation(libs.bundles.androidx.media3)
    implementation(libs.androidx.work)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logger)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.negotiation)
    implementation(libs.ktor.client.auth)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android.compose)
    implementation(libs.koin.android.work)

    implementation(libs.kotlin.immutable.collections)
    implementation(libs.kotlinx.serialization.proto)
    implementation(libs.kotlinx.datetime)
    implementation(libs.qr.compose)

    implementation(libs.flagkit)
    implementation(libs.timber)

    debugImplementation(libs.androidx.compose.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
