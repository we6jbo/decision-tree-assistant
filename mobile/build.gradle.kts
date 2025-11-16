// MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json
// app/build.gradle
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.we6jbo.decision_tree_assistant" //#viar
  compileSdk = 34

  defaultConfig {
    applicationId = "com.we6jbo.decision_tree_assistant" //#viar
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "0.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    // ... other android settings like compileSdk, defaultConfig ...

    packaging {
      resources {
        excludes += listOf("META-INF/INDEX.LIST", "META-INF/DEPENDENCIES")
      }
    }
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
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  // existing ones...
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")

  implementation("com.google.android.gms:play-services-auth:21.2.0")

  // Google API Client & Gmail
  implementation("com.google.api-client:google-api-client-android:1.34.0")
  implementation("com.google.http-client:google-http-client-gson:1.43.3")
  implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")

  // ... other dependencies ...

  // Dependency required for com.google.api.client.extensions.android.http.AndroidHttp
  implementation("com.google.http-client:google-http-client-android:1.44.1")

  // Also ensure you have the core api client and gson factory dependencies
  implementation("com.google.api-client:google-api-client:2.4.0")
  implementation("com.google.http-client:google-http-client-gson:1.44.1")
}


