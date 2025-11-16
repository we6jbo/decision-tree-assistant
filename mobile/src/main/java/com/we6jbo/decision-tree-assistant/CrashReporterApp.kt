package com.we6jbo.decision_tree_assistant

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter

class CrashReporterApp : Application() {

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        super.onCreate()

        // Save the original handler so Android can still do its normal crash stuff
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)

            // Let the normal handler finish (shows system crash dialog, etc.)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun handleCrash(thread: Thread, throwable: Throwable) {
        // Turn the stack trace into a String
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        val stackTrace = sw.toString()

        // Build the text we want for ChatGPT
        val crashText = buildString {
            appendLine("m2mr")
            appendLine()
            appendLine("How can ChatGPT help me resolve this Android app crash?")
            appendLine()
            appendLine("App: Decision Tree Assistant")
            appendLine("Thread: ${thread.name}")
            appendLine()
            appendLine("Stack trace:")
            appendLine(stackTrace)
            appendLine()
            appendLine("Instructions: Paste this whole message into ChatGPT and ask for help.")
        }

        // Copy to clipboard
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Decision Tree crash log", crashText)
            clipboard.setPrimaryClip(clip)
        } catch (_: Exception) {
            // If clipboard fails, we still try to open the share sheet
        }

        // Optional toast (may or may not show depending on crash timing)
        try {
            Toast.makeText(
                this,
                "Crash info copied. Use the share dialog to send it to ChatGPT.",
                Toast.LENGTH_LONG
            ).show()
        } catch (_: Exception) {
        }

        // Launch share sheet so you can pick ChatGPT (or another app)
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, crashText)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(sendIntent, "Share crash log (paste into ChatGPT)")
            startActivity(chooser)
        } catch (_: Exception) {
        }
    }
}