package com.we6jbo.decision_tree_assistant

import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.util.Base64

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.GmailScopes

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class GmailReader(private val context: Context, private val account: GoogleSignInAccount) {

    private val gmailService: Gmail by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(GmailScopes.GMAIL_READONLY)
        ).apply {
            selectedAccount = account.account
        }

        Gmail.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("decision-tree-assistant")
            .build()
    }

    /**
     * Find the latest message sent TO we6jbo+decisiontree@gmail.com
     * with subject exactly matching the pattern:
     *
     *   "dt-out RQ:<requestId>"
     *
     * and return its plain-text body.
     *
     * If the task hangs or throws, we:
     *  - Put a debug message into the clipboard.
     *  - Open the Android share sheet so you can send it to ChatGPT.
     *  - Return null instead of crashing.
     */
    fun fetchLatestDecisionTreeReply(requestId: String): String? {
        val executor = Executors.newSingleThreadExecutor()
        val task = Callable<String?> {
            val query = """to:we6jbo+decisiontree@gmail.com subject:"dt-out RQ:$requestId""""

            val list = gmailService.users().messages().list("me")
                .setQ(query)
                .setMaxResults(1L)
                .execute()

            val messages = list.messages ?: return@Callable null
            if (messages.isEmpty()) return@Callable null

            val msgId = messages[0].id
            val fullMessage: Message = gmailService.users().messages().get("me", msgId)
                .setFormat("full")
                .execute()

            extractPlainTextBody(fullMessage)
        }

        return try {
            executor.submit(task).get(15, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            val description =
                "fetchLatestDecisionTreeReply timed out after 15 seconds. RQ:$requestId"
            copyToClipboardAndOpenShare(
                code = "AQeD",
                description = description
            )
            null
        } catch (e: Exception) {
            val description = buildString {
                append("fetchLatestDecisionTreeReply hit an exception: ")
                append(e.javaClass.simpleName)
                e.message?.let {
                    append(" - ")
                    append(it)
                }
                append(" (RQ:")
                append(requestId)
                append(")")
            }
            copyToClipboardAndOpenShare(
                code = "WEC2",
                description = description
            )
            null
        } finally {
            executor.shutdownNow()
        }
    }

    /**
     * Find the latest Raspberry Pi status message:
     *
     *   Subject starts with "statusinfo"
     *
     * (e.g. "statusinfo 2025-11-17-20 ...")
     *
     * and return its plain-text body.
     *
     * Same hang / exception behavior as the DecisionTree reply method.
     */
    fun fetchLatestStatusInfo(): String? {
        val executor = Executors.newSingleThreadExecutor()
        val task = Callable<String?> {
            val query = """to:we6jbo+decisiontree@gmail.com subject:statusinfo"""

            val list = gmailService.users().messages().list("me")
                .setQ(query)
                .setMaxResults(1L)
                .execute()

            val messages = list.messages ?: return@Callable null
            if (messages.isEmpty()) return@Callable null

            val msgId = messages[0].id
            val fullMessage: Message = gmailService.users().messages().get("me", msgId)
                .setFormat("full")
                .execute()

            extractPlainTextBody(fullMessage)
        }

        return try {
            executor.submit(task).get(15, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            val description = "fetchLatestStatusInfo timed out after 15 seconds."
            copyToClipboardAndOpenShare(
                code = "AQeD",
                description = description
            )
            null
        } catch (e: Exception) {
            val description = buildString {
                append("fetchLatestStatusInfo hit an exception: ")
                append(e.javaClass.simpleName)
                e.message?.let {
                    append(" - ")
                    append(it)
                }
            }
            copyToClipboardAndOpenShare(
                code = "WEC2",
                description = description
            )
            null
        } finally {
            executor.shutdownNow()
        }
    }

    /**
     * Extract a simple plain-text body from a Gmail Message.
     * This is simplified and may need tweaks for complex MIME structures.
     */
    private fun extractPlainTextBody(message: Message): String {
        val payload = message.payload ?: return ""

        // 1) Try the top-level body (if text/plain and data present)
        payload.body?.data?.let { data ->
            val decoded = String(Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP))
            if (decoded.isNotBlank()) return decoded
        }

        // 2) Otherwise, search parts for text/plain
        val parts = payload.parts ?: return ""
        for (part in parts) {
            val mimeType = part.mimeType ?: ""
            if (mimeType.startsWith("text/plain") && part.body?.data != null) {
                return String(
                    Base64.decode(part.body.data, Base64.URL_SAFE or Base64.NO_WRAP)
                )
            }
        }

        // 3) Fallback: empty
        return ""
    }

    /**
     * Build the debug string, copy it to the clipboard, and open a standard
     * Android "Send to..." sheet so you can choose ChatGPT (or another app)
     * on your phone and paste.
     */
    private fun copyToClipboardAndOpenShare(code: String, description: String) {
        val message = "ChatGPT, could you please debug my code $code $description"

        // Put text into clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ChatGPT Debug", message)
        clipboard.setPrimaryClip(clip)

        // Open share sheet (Send to...)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(sendIntent, "Send to ChatGPT or another app")
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            // If we can't start the activity, we still have the text in the clipboard.
        }
    }
}