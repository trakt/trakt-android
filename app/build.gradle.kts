import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

private val localProperties = gradleLocalProperties(rootDir, providers)

android {
    namespace = "tv.trakt.trakt"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "tv.trakt.trakt"
        manifestPlaceholders["applicationName"] = "Trakt"

        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TRAKT_API_KEY", localProperties.getProperty("TRAKT_API_KEY"))
        buildConfigField("String", "TRAKT_API_SECRET", localProperties.getProperty("TRAKT_API_SECRET"))
        buildConfigField("String", "YOUNIFY_API_KEY", localProperties.getProperty("YOUNIFY_API_KEY"))
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")

        ndk {
            debugSymbolLevel = "SYMBOL_TABLE"
        }
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
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("playstore") {
            dimension = "version"
        }
        create("internal") {
            dimension = "version"
            applicationIdSuffix = ".v3"
            manifestPlaceholders["applicationName"] = "Trakt Internal"
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
    implementation(project(":resources"))
    implementation(project(":common"))
    implementation(project(":tv"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.foundation)
    implementation(libs.android.material)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.work)
    implementation(libs.android.billing)

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

    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.coil.svg)

    // Android Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.tooling.preview)
    debugImplementation(libs.androidx.compose.tooling)

    implementation(libs.kotlinx.serialization.proto)
    implementation(libs.kotlin.immutable.collections)
    implementation(libs.timber)
    implementation(libs.phoenix)
    implementation(libs.younify)

    // Testing

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Custom Tasks

// Execute the task immediately during Gradle sync
afterEvaluate {
    val versionCode = libs.versions.versionCode.get()
    val changelogDir = file("${rootProject.projectDir}/fastlane/metadata/android/en-US/changelogs")
    val expectedFileName = "$versionCode.txt"
    val expectedFile = File(changelogDir, expectedFileName)

    if (!changelogDir.exists()) {
        changelogDir.mkdirs()
    }

    val existingFiles = changelogDir.listFiles { file ->
        file.extension == "txt" && file.name != "default.txt"
    }

    if (existingFiles != null && existingFiles.isNotEmpty()) {
        val existingFile = existingFiles.first()
        if (existingFile.name != expectedFileName) {
            existingFile.renameTo(expectedFile)
            // Add renamed file to git

        }
        existingFiles.drop(1).forEach { it.delete() }
    } else if (!expectedFile.exists()) {
        expectedFile.writeText("This update includes general improvements that make Trakt faster, smoother, and more reliable. Enjoy!\n")
    }

    Runtime.getRuntime().exec(arrayOf("git", "add", expectedFile.absolutePath), null, rootProject.projectDir)
}

