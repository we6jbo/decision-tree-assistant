// Notes to ChatGPT, put CrashReporterApp in the exception block for this file.
// Then we do not need the file CrashReporterApp.
// do not modify the AndroidManifestxml - 11-16-2025 at 10:40AM

package com.we6jbo.decision_tree_assistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var signedInAccount: GoogleSignInAccount? = null

    private lateinit var btnConnectGmail: Button
    private lateinit var txtAccount: TextView
    private lateinit var editQuestion: EditText
    private lateinit var btnSend: Button
    private lateinit var txtRequestId: TextView
    private lateinit var btnCheckAnswer: Button
    private lateinit var btnCopyForChatGPT: Button
    private lateinit var txtAnswer: TextView
    private lateinit var txtStatusInfo: TextView

    private var currentRequestId: String? = null

    // Debug tracking
    private var lastSendTo: String? = null
    private var lastSendSubject: String? = null
    private var lastSendBody: String? = null
    private var lastCheckInfo: String? = null
    private var lastErrorMessage: String? = null

    // Hang / task tracking for Gmail check
    @Volatile
    private var currentCheckThread: Thread? = null

    @Volatile
    private var isCheckCancelledByHang: Boolean = false

    private val hangHandler = Handler(Looper.getMainLooper())

    // 30 seconds timeout for "hang" detection (adjust if you want)
    private val HANG_TIMEOUT_MS = 30_000L

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            signedInAccount = task.result
            txtAccount.text = "Connected as: ${signedInAccount?.email}"
        } else {
            txtAccount.text = "Sign-in failed"
            lastErrorMessage = "Google Sign-In failed: ${task.exception?.message}"
            // Non-fatal exception-style handling (optional; no actual crash here)
            handleNonFatalException(
                "Google Sign-In failed in signInLauncher callback. " +
                        "Message: ${task.exception?.message}"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnectGmail = findViewById(R.id.btnConnectGmail)
        txtAccount = findViewById(R.id.txtAccount)
        editQuestion = findViewById(R.id.editQuestion)
        btnSend = findViewById(R.id.btnSend)
        txtRequestId = findViewById(R.id.txtRequestId)
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer)
        btnCopyForChatGPT = findViewById(R.id.btnCopyForChatGPT)
        txtAnswer = findViewById(R.id.txtAnswer)
        txtStatusInfo = findViewById(R.id.txtStatusInfo)

        setupGoogleSignIn()

        // Load any previously saved Request ID so it persists across app restarts
        currentRequestId = RequestIdStore.loadRequestId(this)
        currentRequestId?.let {
            txtRequestId.text = "Request ID: $it"
        }

        btnConnectGmail.setOnClickListener {
            try {
                signIn()
            } catch (e: Exception) {
                lastErrorMessage = "Exception during signIn(): ${e.message}"
                handleNonFatalException(
                    "Exception during signIn() in btnConnectGmail click handler.\n" +
                            e.stackTraceToString()
                )
            }
        }

        btnSend.setOnClickListener {
            try {
                sendDecisionTreeRequest()
            } catch (e: Exception) {
                lastErrorMessage = "Exception in sendDecisionTreeRequest(): ${e.message}"
                handleNonFatalException(
                    "Exception while sending Decision Tree request.\n" +
                            e.stackTraceToString()
                )
            }
        }

        btnCheckAnswer.setOnClickListener {
            try {
                checkForAnswer()
            } catch (e: Exception) {
                lastErrorMessage = "Exception in checkForAnswer(): ${e.message}"
                handleNonFatalException(
                    "Exception starting checkForAnswer().\n" +
                            e.stackTraceToString()
                )
            }
        }

        btnCopyForChatGPT.setOnClickListener {
            try {
                copyDebugInfoToClipboard()
            } catch (e: Exception) {
                lastErrorMessage = "Exception in copyDebugInfoToClipboard(): ${e.message}"
                handleNonFatalException(
                    "Exception while copying debug info manually.\n" +
                            e.stackTraceToString()
                )
            }
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(GmailScopes.GMAIL_READONLY)
                // later we can add GMAIL_SEND if we move sending into the app
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            signedInAccount = account
            txtAccount.text = "Connected as: ${account.email}"
        }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        // You can swap to signInLauncher.launch(signInIntent) if you prefer.
        startActivity(signInIntent)
    }

    private fun sendDecisionTreeRequest() {
        val questionText = editQuestion.text.toString().trim()
        if (questionText.isEmpty()) {
            Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show()
            return
        }

        if (signedInAccount == null) {
            Toast.makeText(this, "Connect Gmail first", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a new time-based Request ID only when SEND is pressed
        val requestId = generateRequestId()
        currentRequestId = requestId
        RequestIdStore.saveRequestId(this, requestId)

        txtRequestId.text = "Request ID: $requestId"
        txtAnswer.text = "No answer yet."
        txtStatusInfo.text = "No statusinfo message yet."

        // Include the RQ in the subject so Pi can echo it back
        val subject = "dt-in RQ:$requestId"

        val body = """
            Request-ID: $requestId

            Question:
            $questionText

            Extra context (optional):
            - Sent from Decision Tree Android app.
        """.trimIndent()

        val to = "master@we6jbobbs.org"

        // Track for debug
        lastSendTo = to
        lastSendSubject = subject
        lastSendBody = body
        lastErrorMessage = null

        // This is wrapped in try/catch by the click listener, and this method itself
        // catches errors from startActivity and treats them as non-fatal.
        sendEmailViaIntent(
            to = to,
            subject = subject,
            body = body
        )
    }

    private fun checkForAnswer() {
        val requestId = currentRequestId
        if (requestId == null) {
            Toast.makeText(this, "No request ID yet", Toast.LENGTH_SHORT).show()
            return
        }

        val account = signedInAccount
        if (account == null) {
            Toast.makeText(this, "Connect Gmail first", Toast.LENGTH_SHORT).show()
            return
        }

        txtAnswer.text = "Checking Gmail for Decision Tree reply..."
        txtStatusInfo.text = "Checking Gmail for Status Info..."

        // Reset hang flags and debug info
        isCheckCancelledByHang = false

        lastCheckInfo = """
            Real Gmail check for Decision Tree reply.
            Gmail account: ${account.email}
            Search query:
              to:we6jbo+decisiontree@gmail.com
              subject:"dt-out RQ:$requestId"
            Using Request-ID: $requestId

            Also checking latest statusinfo message.
        """.trimIndent()

        val worker = Thread {
            try {
                val reader = GmailReader(this, account)
                val body = reader.fetchLatestDecisionTreeReply(requestId)
                val statusBody = reader.fetchLatestStatusInfo()

                runOnUiThread {
                    if (isCheckCancelledByHang) {
                        // Task was cancelled by hang detector; do not update UI further.
                        return@runOnUiThread
                    }

                    if (body.isNullOrBlank()) {
                        txtAnswer.text = "No dt-out reply found yet for RQ:$requestId."
                    } else {
                        txtAnswer.text = body
                    }

                    if (statusBody.isNullOrBlank()) {
                        txtStatusInfo.text = "No statusinfo message found."
                    } else {
                        txtStatusInfo.text = statusBody
                    }
                }
            } catch (e: Exception) {
                lastErrorMessage = "Error reading Gmail: ${e.message}"
                runOnUiThread {
                    if (isCheckCancelledByHang) {
                        // Task was cancelled by hang detector; do not update UI further.
                        return@runOnUiThread
                    }
                    txtAnswer.text = "Error reading Gmail: ${e.message}"
                    // Treat this as a non-fatal exception for crash reporting.
                    handleNonFatalException(
                        "Exception inside Gmail check thread while calling " +
                                "GmailReader.fetchLatestDecisionTreeReply() / fetchLatestStatusInfo().\n" +
                                e.stackTraceToString()
                    )
                }
            }
        }

        currentCheckThread = worker
        worker.start()

        // Hang detection: if this thread is still alive after HANG_TIMEOUT_MS,
        // treat it as "hung", stop doing that task, and report via AQeD.
        hangHandler.postDelayed({
            val t = currentCheckThread
            if (t != null && t.isAlive && !isCheckCancelledByHang) {
                isCheckCancelledByHang = true
                try {
                    t.interrupt()
                } catch (_: Exception) {
                    // Ignore; we just try to nudge it.
                }

                txtAnswer.text =
                    "The Gmail check seems to be taking too long and was stopped."

                handleHang(
                    "The Gmail check for Decision Tree reply appeared to hang. " +
                            "Thread was still alive after ${HANG_TIMEOUT_MS}ms.\n" +
                            "LastCheckInfo:\n$lastCheckInfo\n\nLastErrorMessage:\n$lastErrorMessage"
                )
            }
        }, HANG_TIMEOUT_MS)
    }

    private fun generateRequestId(): String {
        // Time-based ID, only captured once when SEND is pressed
        val sdf = SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.US)
        return sdf.format(Date())
    }

    /**
     * V1: use the system email client (e.g. Gmail) to send.
     * This does not store passwords, and keeps things simple.
     */
    private fun sendEmailViaIntent(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send email with"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            lastErrorMessage = "No email app found: ${e.message}"
            // Treat as a non-fatal exception for reporting.
            handleNonFatalException(
                "Exception while trying to launch email app via Intent.ACTION_SEND.\n" +
                        e.stackTraceToString()
            )
        }
    }

    /**
     * Build a reusable debug bundle with all the useful state.
     */
    private fun buildDebugBundle(): String {
        return buildString {
            appendLine("=== Decision Tree Android App Debug Info ===")
            appendLine("Timestamp: ${Date()}")
            appendLine("Signed-in Gmail account: ${signedInAccount?.email ?: "None"}")
            appendLine("Current Request ID: ${currentRequestId ?: "None"}")
            appendLine()
            appendLine("--- Last Outgoing Email ---")
            appendLine("To: ${lastSendTo ?: "N/A"}")
            appendLine("Subject: ${lastSendSubject ?: "N/A"}")
            appendLine("Body:")
            appendLine(lastSendBody ?: "N/A")
            appendLine()
            appendLine("--- Last Check for Answer Call ---")
            appendLine(lastCheckInfo ?: "No check has been performed yet.")
            appendLine()
            appendLine("--- Last Visible Answer Text in App ---")
            appendLine(txtAnswer.text?.toString() ?: "N/A")
            appendLine()
            appendLine("--- Last Status Info Text in App ---")
            appendLine(txtStatusInfo.text?.toString() ?: "N/A")
            appendLine()
            appendLine("--- Last Error (if any) ---")
            appendLine(lastErrorMessage ?: "None recorded.")
            appendLine()
            appendLine("End of Decision Tree debug bundle.")
        }
    }

    /**
     * Build a crash / hang report message for ChatGPT, with code and description.
     */
    private fun buildCrashMessage(code: String, description: String): String {
        val header = "ChatGPT, could you please debug my code $code"
        return buildString {
            appendLine(header)
            appendLine()
            appendLine("Description of what happened:")
            appendLine(description)
            appendLine()
            appendLine(buildDebugBundle())
        }
    }

    /**
     * Core helper that:
     * 1) Copies the crash / hang message to the clipboard.
     * 2) Opens a SEND chooser so you can select ChatGPT (or any app) and paste.
     */
    private fun copyCrashInfoAndOpenShare(code: String, description: String) {
        val text = buildCrashMessage(code, description)

        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DecisionTreeCrash", text)
        cm.setPrimaryClip(clip)

        Toast.makeText(
            this,
            "Crash / hang info copied to clipboard for ChatGPT",
            Toast.LENGTH_SHORT
        ).show()

        // Open the "send to" / share sheet so you can choose ChatGPT on your phone.
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Send to ChatGPT or another app"))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open share sheet: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Hang handler: use code AQeD.
     */
    private fun handleHang(description: String) {
        copyCrashInfoAndOpenShare("AQeD", description)
    }

    /**
     * Non-fatal exception handler: use code WEC2.
     */
    private fun handleNonFatalException(description: String) {
        copyCrashInfoAndOpenShare("WEC2", description)
    }

    /**
     * Manual debug bundle copier (your existing button).
     */
    private fun copyDebugInfoToClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val debugText = buildString {
            appendLine("ChatGPT, refer to my reference on my gmail decision tree project")
            appendLine()
            appendLine(buildDebugBundle())
        }

        val clip = ClipData.newPlainText("DecisionTreeDebug", debugText)
        cm.setPrimaryClip(clip)

        Toast.makeText(this, "Debug info copied for ChatGPT", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Simple persistent store for the last Request ID (RQ).
 * This keeps the time-based RQ as a "variable" that survives app restarts,
 * and is only changed when the user presses SEND.
 */
object RequestIdStore {
    private const val PREF_NAME = "decision_tree_prefs"
    private const val KEY_REQUEST_ID = "last_request_id"

    fun saveRequestId(context: Context, requestId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REQUEST_ID, requestId).apply()
    }

    fun loadRequestId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REQUEST_ID, null)
    }

    fun clearRequestId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_REQUEST_ID).apply()
    }
}