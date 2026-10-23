import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

// The shared Render API is the default endpoint used by repository builds,
// physical devices and GitHub Actions.
//
// Individual developers can temporarily override the URL by adding:
//
// api.base.url=http://10.0.2.2:5154/
//
// to local.properties when testing the ASP.NET Core API locally.
val localApiBaseUrl = localProperties.getProperty("api.base.url")
val sharedApiBaseUrl = providers.gradleProperty("CHOMMIEHUB_API_BASE_URL").orNull
val configuredApiBaseUrl = localApiBaseUrl ?: sharedApiBaseUrl ?: "https://chommiehub-api.onrender.com/"
val apiBaseUrl = if (configuredApiBaseUrl.endsWith("/")) configuredApiBaseUrl else "$configuredApiBaseUrl/"

val managerAccommodationId = localProperties.getProperty("manager.accommodation.id", "")

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

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "MANAGER_ACCOMMODATION_ID", "\"$managerAccommodationId\"")

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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(libs.firebase.auth)
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // Firebase Cloud Messaging for real-time push notifications
    implementation("com.google.firebase:firebase-messaging:25.0.1")

    implementation("androidx.biometric:biometric:1.1.0")

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.recyclerview)

    implementation("org.osmdroid:osmdroid-android:6.1.20")

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}