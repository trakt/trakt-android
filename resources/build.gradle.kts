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

    kotlin {
        jvmToolchain(11)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// The strings.xml file is used as the source of truth for all translations.
// This task copies the English version to the values dir, which is used as default.
copy {
    from("src/main/res/values-en/strings.xml")
    into("src/main/res/values")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
