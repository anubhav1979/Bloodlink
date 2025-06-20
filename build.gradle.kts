
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    // ...other plugins
//    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}

