plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.anix.android.anixplayer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.anix.android.anixplayer"
        minSdk = 36
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.anix.android.anixplayer.vidflow.CustomTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.camera)
    implementation(libs.bundles.compose.adaptive)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.room)
    implementation(libs.hilt)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.material)
    implementation(libs.moshi.kotlin)
    implementation(libs.bundles.networking)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.bundles.compose.debug)
    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
    ksp(libs.hilt.compiler)
    
    // Feature Modules
    implementation(project(":feature:gallery"))
    implementation(project(":feature:video-player"))
}
