# ──────────────────────────────────────────────────────────────────────────────
fun submit(goal: String, cons: List<String>) {
val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
val api = ApiFactory.create(base(app))
val auth = token(app)
val tree = DecisionTree(
id = java.util.UUID.randomUUID().toString(),
created_at = Instant.now().toString(),
goal = goal,
constraints = cons,
nodes = listOf(Node("root", "Start toward: $goal", 0.5, emptyList()))
)
androidx.lifecycle.viewModelScope.launch {
try { api.heartbeat(mapOf("device_id" to "galaxy-phone", "device_type" to "phone", "app_version" to "0.1.0"), auth) } catch (_: Exception) {}
api.ingest(tree, auth)
}
}
}


# ──────────────────────────────────────────────────────────────────────────────
# wear/src/main/AndroidManifest.xml
# ──────────────────────────────────────────────────────────────────────────────
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
<application android:label="DT Wear">
<activity android:name=".VoiceActivity">
<intent-filter>
<action android:name="android.intent.action.MAIN"/>
<category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>
</activity>
</application>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-feature android:name="android.hardware.type.watch"/>
</manifest>


# ──────────────────────────────────────────────────────────────────────────────
# wear/src/main/java/com/jeremiah/dt/wear/VoiceActivity.kt
# ──────────────────────────────────────────────────────────────────────────────
package com.jeremiah.dt.wear
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.wearable.*


class VoiceActivity: Activity() {
override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
val intent = Intent("android.speech.action.RECOGNIZE_SPEECH")
intent.putExtra("android.speech.extra.PROMPT", "Say your goal")
startActivityForResult(intent, 100)
}
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
super.onActivityResult(requestCode, resultCode, data)
if (requestCode == 100 && resultCode == RESULT_OK) {
val text = data?.getStringArrayListExtra("android.speech.extra.RESULTS")?.firstOrNull() ?: return
val path = "/new-goal"
val req = PutDataMapRequest.create(path).apply { dataMap.putString("goal", text) }.asPutDataRequest().setUrgent()
Wearable.getDataClient(this).putDataItem(req)
finish()
} else finish()
}
}


# ──────────────────────────────────────────────────────────────────────────────
# README (quick start)
# ──────────────────────────────────────────────────────────────────────────────
1) File ▸ New ▸ Import Project, choose this folder in Android Studio.
2) Run **mobile** on your Samsung phone. Tap **Settings**, verify Base URL is `http://192.168.8.109:8088/` and the token starts with `Bearer ` and your long hex string. Save.
3) On the phone, enter a goal and press **Send to Pi** (this POSTs `/heartbeat` and `/ingest`).
4) Run **wear** on your Samsung watch. Speak a goal; it will sync to the phone (the phone code can forward it to the Pi—hook into `PlanVM.submit`).
5) Your Pi must be on SSID `jeremiahoneal` and service listening on port 8088.


# Notes
- Your token is pre-filled for convenience in SettingsActivity; you can rotate it on the Pi and paste the new one.
- Cleartext HTTP is allowed only to `192.168.8.109` for dev. For prod, use HTTPS or a reverse proxy.
- Never commit real tokens to public repos.
