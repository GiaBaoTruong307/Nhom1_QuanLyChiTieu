plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // luôn để sau cùng
}

android {
    namespace = "com.example.nhom1_quanlychitieu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nhom1_quanlychitieu"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            pickFirsts += "META-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    // Android UI & Navigation
    implementation(libs.appcompat)
    implementation(libs.material) // giữ lại dòng từ Version Catalog
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // RecyclerView & CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Firebase BoM – quản lý version tập trung
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // AndroidX KTX & Fragment
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.fragment:fragment:1.6.2")

    // Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
