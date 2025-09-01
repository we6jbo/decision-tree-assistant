package com.jeremiah.dt.mobile

import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
