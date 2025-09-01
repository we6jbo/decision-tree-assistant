package com.jeremiah.dt.mobile

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

class SendWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    private val client = OkHttpClient()

    override fun doWork(): Result {
        val goal = inputData.getString("goal") ?: "Unset"
        val json = """
            {
              "id": "local-${System.currentTimeMillis()}",
              "user": "jeremiah",
              "created_at": "${Instant.now()}",
              "goal": "${goal.replace("\"","'")}",
              "constraints": [],
              "facts": {},
              "nodes": [{"id":"root","text":"Start","score":0.0,"children":[]}],
              "edges": []
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder()
            .url("http://192.168.8.109:8088/ingest")
            .addHeader("Authorization", "Bearer 74869ecf04d620fd9cffc29643647fdb6544270a6c8f7bdb2016362b3a4d8f97")
            .post(body)
            .build()

        return try {
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) Result.success() else Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
