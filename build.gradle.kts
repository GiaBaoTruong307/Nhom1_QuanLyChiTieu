// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

// Cấu hình Google Services
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        // Phiên bản mới nhất của Google Services plugin
        classpath ("com.google.gms:google-services:4.4.0")
    }
}