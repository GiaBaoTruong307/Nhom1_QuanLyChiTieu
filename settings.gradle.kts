pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // JitPack để dùng các thư viện GitHub như MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    // Ngăn dùng repo cục bộ trong từng project
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// Tên của project gốc
rootProject.name = "Nhom1_QuanLyChiTieu"

// Thêm module app
include(":app")
