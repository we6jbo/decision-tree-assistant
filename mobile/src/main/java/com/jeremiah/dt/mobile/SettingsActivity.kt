// MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json
package com.jeremiah.dt.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple container for the PreferenceFragment
        val containerId = 1001
        val frame = FrameLayout(this).apply { id = containerId }
        setContentView(frame)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, SettingsFragment())
                .commit()
        }

        supportActionBar?.title = "Settings"
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Navigate to About screen
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), AboutActivity::class.java))
                true
            }
        }
    }
}

