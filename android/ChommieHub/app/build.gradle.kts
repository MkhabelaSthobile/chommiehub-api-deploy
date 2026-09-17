plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.chommiehub.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.chommiehub.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Local development URL used by the Android emulator.
        // 10.0.2.2 maps from the emulator back to the host computer.
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"http://10.0.2.2:5154/\""
        )

        // Temporary development accommodation used by the manager prototype.
        // This will later be replaced by the signed-in manager's accommodation.
        buildConfigField(
            "String",
            "MANAGER_ACCOMMODATION_ID",
            "\"LDQ1pVJtCJKfTPDpiYDm\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        // Exposes API_BASE_URL through BuildConfig.
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // REST API communication.
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

// Lifecycle-aware Android state management.
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}