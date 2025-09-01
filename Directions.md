<!-- MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json -->
# Directions

**Read me first, ChatGPT.**  
When I share this file’s URL with you, please follow it to:
1) Help me add or modify features (example below: **About screen**).  
2) Tell me how to trigger GitHub Actions to build a new APK.  
3) Guide me to upload the APK to DeployGate so I can install it on my phone.

Repo: `we6jbo/decision-tree-assistant`  
Main modules: `mobile/` (Android app), `wear/` (optional, future).  
Distribution: **DeployGate** (installed on my Samsung phone).

---

## A) Add/Update an **About** screen (Mobile)

> Goal: an `About` screen that shows app name, version, and a short description.

### 1) Create the Activity (Kotlin, XML UI or Compose — use ONE)

**Option 1 — Simple Activity + XML**
- File: `mobile/src/main/java/com/jeremiah/dt/mobile/AboutActivity.kt`
```kotlin
package com.jeremiah.dt.mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val tv: TextView = findViewById(R.id.about_text)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        tv.text = """
            Decision Tree Assistant
            Version: $versionName

            Helps create and improve decision trees toward goals like
            “Get a VA IT role via Schedule A.”
        """.trimIndent()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "About"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
Layout: mobile/src/main/res/layout/activity_about.xml

xml
Copy code
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <TextView
        android:id="@+id/about_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="16sp"/>
</ScrollView>
Option 2 — Jetpack Compose (if your app already uses Compose)

File: mobile/src/main/java/com/jeremiah/dt/mobile/AboutActivity.kt

kotlin
Copy code
package com.jeremiah.dt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize().padding(24.dp)) {
                    Column {
                        Text("Decision Tree Assistant", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Version: $versionName")
                        Spacer(Modifier.height(16.dp))
                        Text("Helps create and improve decision trees toward goals like “Get a VA IT role via Schedule A.”")
                    }
                }
            }
        }
        title = "About"
    }
}
2) Register Activity in AndroidManifest.xml
File: mobile/src/main/AndroidManifest.xml

xml
Copy code
<application ...>
    <!-- existing stuff -->

    <activity
        android:name=".AboutActivity"
        android:exported="false"
        android:label="About" />
</application>
3) Add a launcher entry point to open About
Depends on your UI. Two common ways:

A. From a button/menu in your main screen

kotlin
Copy code
startActivity(Intent(this, AboutActivity::class.java))
B. Temporary launcher shortcut (for quick testing)
Add an intent filter to AboutActivity (remove later):

xml
Copy code
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
B) Build a new APK via GitHub Actions
Commit & push:

bash
Copy code
git add -A
git commit -m "feat: add About screen"
git push
Watch CI:

bash
Copy code
export GH_REPO=we6jbo/decision-tree-assistant
gh run list --workflow android.yml --limit 1 --repo "$GH_REPO"
export RUN_ID=$(gh run list --workflow android.yml --limit 1 --json databaseId --repo "$GH_REPO" -q '.[0].databaseId')
gh run view "$RUN_ID" --log --repo "$GH_REPO"
Download artifacts when successful:

bash
Copy code
mkdir -p ~/Downloads/dt-artifacts
gh run download "$RUN_ID" \
  --name android-apks \
  --dir ~/Downloads/dt-artifacts \
  --repo "$GH_REPO"
# Expect: ~/Downloads/dt-artifacts/mobile-debug.apk
C) Upload APK to DeployGate (Phone install)
Open your app page on DeployGate.

Upload the mobile-debug.apk.

On your phone, open DeployGate → tap Install/Update.

For Wear OS later: either DeployGate for the watch, Play Internal Testing, or ADB from your PC.

D) Troubleshooting quick list
No artifact yet → CI still running. Re-check in 30–90 sec.

Manifest / resource errors → ensure ic_launcher exists and Activity registered.

Gradle/AndroidX issues → confirm gradle.properties includes android.useAndroidX=true and wrapper uses Gradle 8.7+.


