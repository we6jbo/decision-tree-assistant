package com.jeremiah.dt.mobile
import android.content.Context
import androidx.work.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class SendWorker(appCtx: Context, params: WorkerParameters) : CoroutineWorker(appCtx, params) {
  override suspend fun doWork(): Result = withContext(IO) {
    val ctx = applicationContext
    val prefs = encryptedPrefs(ctx)
    val base = prefs.getString("baseUrl", "http://192.168.8.109:8088/")!!
    val token = prefs.getString("token", "")!!
    if (token.isBlank()) return@withContext Result.retry()

    val api = ApiFactory.create(base)
    val dao = Queue.get(ctx).dao()
    val json = Json

    var attempts = 0
    while (attempts < 20) {
      val ev = dao.peek() ?: return@withContext Result.success()
      try {
        when (ev.kind) {
          "heartbeat" -> api.heartbeat(mapOf("device_id" to "galaxy-phone", "device_type" to "phone", "app_version" to "0.1.0"), token)
          "ingest" -> {
            val t = json.decodeFromString(DecisionTree.serializer(), ev.payloadJson)
            api.ingest(t, token)
          }
          "improve" -> {
            val body = json.decodeFromString<Map<String, @JvmSuppressWildcards Any>>(ev.payloadJson)
            api.improve(body, token)
          }
        }
        dao.delete(ev.id) // success
      } catch (e: Exception) {
        // network / auth / server error -> back off & retry later
        attempts++
        delay(1500L * attempts)
      }
    }
    Result.retry()
  }

  companion object {
    fun schedule(ctx: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      val req = PeriodicWorkRequestBuilder<SendWorker>(15, TimeUnit.MINUTES) // min period
        .setConstraints(constraints)
        .addTag("queue-drain")
        .build()
      WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("queue-drain", ExistingPeriodicWorkPolicy.UPDATE, req)

      // also kick an immediate run when we enqueue something:
      val now = OneTimeWorkRequestBuilder<SendWorker>().setConstraints(constraints).build()
      WorkManager.getInstance(ctx).enqueue(now)
    }
  }
}

