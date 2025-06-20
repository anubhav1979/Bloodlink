import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // If using Compose with Kotlin 2.0+:
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.gms.google-services")
}

//buildscript{
//    repositories {
//        google()  // Must come first
//        mavenCentral()
//    }
//    dependencies {
//        // This is the correct place for classpath declarations
//        classpath (libs.gradle.v810)
//        // Other classpath dependencies...
//    }
//}

android {
    namespace = "com.example.bloodlink" // <-- Add this line
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        compose = true
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

    defaultConfig {
        applicationId = "com.example.bloodlink"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//        isCoreLibraryDesugaringEnabled = true
//
//
//    }

    // ... rest of your configuration ...
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.location)
    implementation(libs.volley)
    implementation(libs.androidx.tools.core)
    implementation(libs.play.services.fitness)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation (libs.google.firebase.auth.ktx)
    implementation (libs.firebase.firestore.ktx)

    // In your app's build.gradle:
    implementation (libs.play.services.auth)
    implementation (libs.firebase.firestore.ktx.v24100)
    implementation (libs.material.v1110)
    implementation (libs.firebase.firestore)
    implementation (libs.places)
    implementation (libs.play.services.location.v2101)
    implementation (libs.okhttp)
    implementation (libs.play.services.location)
    implementation (libs.places.v340)
    implementation (libs.okhttp.v4120)
    implementation (libs.places.v330)
    implementation (libs.places) // Use latest version
    implementation (libs.play.services.base)
    implementation (libs.androidx.core.ktx.v1120)
    coreLibraryDesugaring (libs.desugar.jdk.libs)
    implementation (libs.play.services.base.v1830)
    implementation (libs.play.services.tasks)
    implementation (libs.play.services.basement)
    coreLibraryDesugaring(libs.desugar.jdk.libs.v203)
    implementation(libs.ui)
    implementation(libs.material3)
    coreLibraryDesugaring(libs.desugar.jdk.libs.v204)

    // Your other dependencies
    implementation(libs.play.services.base)
    implementation(libs.play.services.tasks)
    implementation(libs.play.services.basement)
    implementation(libs.google.firebase.auth)
    implementation (libs.places)
    implementation (libs.retrofit)

    // Gson Converter (for JSON parsing)
    implementation (libs.converter.gson)

    // OkHttp Logging (optional but helpful for debugging)
    implementation (libs.logging.interceptor)
    implementation(libs.firebase.firestore.v24101)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.androidx.recyclerview)

}






