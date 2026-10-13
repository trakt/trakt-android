plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "tv.trakt.trakt.resources"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
        }
    }

    sourceSets {
        getByName("main") {
            res.directories.add("src/main/res-halloween")
        }
    }

    kotlin {
        jvmToolchain(11)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val validateStringPlaceholders = tasks.register<ValidateStringPlaceholdersTask>("validateStringPlaceholders") {
    resDir.from(layout.projectDirectory.dir("src/main/res"))
    ignoredFormatKeys.addAll("match_pill_aria_label", "yir_2026_rarity")
}

tasks.named("preBuild") {
    dependsOn(validateStringPlaceholders)
}
