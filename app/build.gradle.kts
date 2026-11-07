plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.remsymes"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.remsymes"
        minSdk = 24
        targetSdk = 37
        versionCode = (project.findProperty("releaseVersionCode") as String?)?.toInt() ?: 1
        versionName = project.findProperty("releaseVersionName") as String? ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Populated from env vars so local/CI-without-secrets builds still produce an
    // (unsigned) release APK instead of failing. CI sets these from repo secrets.
    val releaseKeystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = false
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.mlkit.text.recognition)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.json)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}