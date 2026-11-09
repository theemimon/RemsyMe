plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.remsy"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "me.remsy"
        // 26+ required by the mlkit-genai-prompt on-device LLM dependency.
        minSdk = 26
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
    implementation(libs.mlkit.genai.prompt)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.json)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}