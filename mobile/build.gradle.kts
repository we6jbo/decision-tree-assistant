plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.kapt")
}

android {
  namespace = "com.jeremiah.dt.mobile"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.jeremiah.dt.mobile"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }

  buildFeatures {
    viewBinding = true
    compose = false
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")

  implementation("androidx.work:work-runtime-ktx:2.9.1")

  // Networking & JSON (simple)
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

  // (Optional) encrypted prefs
  implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
