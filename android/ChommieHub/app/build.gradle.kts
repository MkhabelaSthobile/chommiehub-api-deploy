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

val localApiBaseUrl = localProperties.getProperty("api.base.url")
val sharedApiBaseUrl = providers.gradleProperty("CHOMMIEHUB_API_BASE_URL").orNull
val configuredApiBaseUrl = localApiBaseUrl ?: sharedApiBaseUrl ?: "http://10.0.2.2:5154/"
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

        // Supplies the REST API base address to Retrofit.
        // local.properties can override the shared Render URL when local API testing is required.
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
    // Core Android user-interface libraries.
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Firebase Authentication and Google Single Sign-On.
    implementation(libs.firebase.auth)
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // Biometric support retained for Final POE preparation.
    implementation("androidx.biometric:biometric:1.1.0")

    // Retrofit REST API communication.
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // Android lifecycle and ViewModel support.
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.recyclerview)

    // OpenStreetMap map support.
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // Local unit testing.
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)

    // Android instrumentation testing.
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}