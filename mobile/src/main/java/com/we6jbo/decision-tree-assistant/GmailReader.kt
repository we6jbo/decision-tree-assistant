package com.we6jbo.decision_tree_assistant

import android.content.Context
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.GmailScopes
import android.util.Base64

class GmailReader(private val context: Context, private val account: GoogleSignInAccount) {

    private val gmailService: Gmail by lazy {
        // Use GoogleAccountCredential to build a Gmail service
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
     * with subject containing "dt-out" and return its plain-text body.
     */
    fun fetchLatestDecisionTreeReply(): String? {
        // Gmail "q" search syntax:
        //   to:we6jbo+decisiontree@gmail.com subject:dt-out
        val query = "to:we6jbo+decisiontree@gmail.com subject:dt-out"

        val list = gmailService.users().messages().list("me")
            .setQ(query)
            .setMaxResults(1L)
            // .setOrderBy("date") <-- REMOVE THIS LINE
            .execute()

        val messages = list.messages ?: return null
        if (messages.isEmpty()) return null

        val msgId = messages[0].id
        val fullMessage: Message = gmailService.users().messages().get("me", msgId)
            .setFormat("full")
            .execute()

        return extractPlainTextBody(fullMessage)
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
}

