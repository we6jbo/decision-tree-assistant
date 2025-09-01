package com.jeremiah.dt.mobile

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
 // simple container for the fragment
        val containerId = 1001
        val frame = android.widget.FrameLayout(this).apply { id = containerId }
        setContentView(frame)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, SettingsFragment())
                .commit()
        }
        supportActionBar?.title = "Settings"
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val title = TextView(this).apply { text = "Decision Tree Assistant" }
        val goal = EditText(this).apply { hint = "Goal (e.g., Get VA IT role)" }
        val submit = Button(this).apply { text = "Submit" }
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)

        root.addView(title)
        root.addView(goal, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(submit)
        setContentView(root)

        submit.setOnClickListener {
            val data = workDataOf("goal" to goal.text.toString())
            val req = OneTimeWorkRequestBuilder<SendWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(this).enqueue(req)
            Toast.makeText(this, "Queued for send", Toast.LENGTH_SHORT).show()
        }
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), AboutActivity::class.java))
                true
            }
        }
    }
