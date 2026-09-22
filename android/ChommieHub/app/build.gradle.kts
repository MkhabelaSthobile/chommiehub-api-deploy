import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use {
            load(it)
        }
    }
}

/*val managerAccommodationId =
    localProperties.getProperty(
        "manager.accommodation.id",
        "NOT_CONFIGURED"
    )*/

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
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

        // Physical device on the same Wi-Fi -> local ASP.NET API on the PC's LAN IP.
        // (Was "http://10.0.2.2:5154/" for the Android Emulator - that address only
        // works inside the emulator's virtual network, not on a real phone.)
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"http://192.168.101.156:5154/\""
        )

        // Manager accommodation ID loaded from local.properties
        buildConfigField(
            "String",
            "MANAGER_ACCOMMODATION_ID",
            "\"BBTdJM7OQqGHPc1j8zFt\""
        )

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
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

    // ---------------------------------------------------------
    // Android
    // ---------------------------------------------------------

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // ---------------------------------------------------------
    // Firebase Authentication
    // ---------------------------------------------------------

    implementation(libs.firebase.auth)

    // Google Sign-In
    implementation(
        "com.google.android.gms:play-services-auth:21.4.0"
    )

    // ---------------------------------------------------------
    // Biometric Authentication
    // ---------------------------------------------------------

    implementation(
        "androidx.biometric:biometric:1.1.0"
    )

    // ---------------------------------------------------------
    // Retrofit / REST API
    // ---------------------------------------------------------

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // ---------------------------------------------------------
    // Lifecycle / ViewModel
    // ---------------------------------------------------------

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ---------------------------------------------------------
    // RecyclerView
    // ---------------------------------------------------------

    implementation(libs.androidx.recyclerview)

    // ---------------------------------------------------------
    // Testing
    // ---------------------------------------------------------

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Maps (OpenStreetMap via osmdroid — no API key required)
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}
