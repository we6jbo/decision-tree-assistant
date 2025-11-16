package com.we6jbo.decision_tree_assistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    private var currentRequestId: String? = null

    // Debug tracking
    private var lastSendTo: String? = null
    private var lastSendSubject: String? = null
    private var lastSendBody: String? = null
    private var lastCheckInfo: String? = null
    private var lastErrorMessage: String? = null

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

        setupGoogleSignIn()

        btnConnectGmail.setOnClickListener {
            signIn()
        }

        btnSend.setOnClickListener {
            sendDecisionTreeRequest()
        }

        btnCheckAnswer.setOnClickListener {
            checkForAnswer()
        }

        btnCopyForChatGPT.setOnClickListener {
            copyDebugInfoToClipboard()
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
        signInLauncher.launch(signInIntent)
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

        val requestId = generateRequestId()
        currentRequestId = requestId
        txtRequestId.text = "Request ID: $requestId"
        txtAnswer.text = "No answer yet."

        val subject = "dt-in"

        val body = """
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

        // Track debug info
        lastCheckInfo = """
            Real Gmail check for Decision Tree reply.
            Gmail account: ${account.email}
            Search query:
              to:we6jbo+decisiontree@gmail.com
              subject:dt-out
            Note: requestId ($requestId) is not yet used in the search.
        """.trimIndent()

        Thread {
            try {
                val reader = GmailReader(this, account)
                val body = reader.fetchLatestDecisionTreeReply()

                runOnUiThread {
                    if (body.isNullOrBlank()) {
                        txtAnswer.text = "No dt-out reply found yet."
                    } else {
                        txtAnswer.text = body
                    }
                }
            } catch (e: Exception) {
                lastErrorMessage = "Error reading Gmail: ${e.message}"
                runOnUiThread {
                    txtAnswer.text = "Error reading Gmail: ${e.message}"
                }
            }
        }.start()
    }

    private fun generateRequestId(): String {
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
        }
    }

    /**
     * Build a clipboard text blob for ChatGPT.
     * Starts with your reference line, then all useful debug info.
     */
    private fun copyDebugInfoToClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val debugText = buildString {
            appendLine("ChatGPT, refer to my reference on my gmail decision tree project")
            appendLine()
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
            appendLine("--- Last Error (if any) ---")
            appendLine(lastErrorMessage ?: "None recorded.")
            appendLine()
            appendLine("End of Decision Tree debug bundle.")
        }

        val clip = ClipData.newPlainText("DecisionTreeDebug", debugText)
        cm.setPrimaryClip(clip)

        Toast.makeText(this, "Debug info copied for ChatGPT", Toast.LENGTH_SHORT).show()
    }
}