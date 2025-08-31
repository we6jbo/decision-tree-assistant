#Jeremiah Burke O'Neal
#Aug 31 2025

// build.gradle.kts (module)
dependencies {
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
implementation("androidx.security:security-crypto:1.1.0-alpha06")
implementation("androidx.work:work-runtime-ktx:2.9.1")
implementation(platform("androidx.compose:compose-bom:2024.10.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
}
@kotlinx.serialization.Serializable
data class Node(val id: String, val text: String, val score: Double? = null, val children: List<String> = emptyList())
@kotlinx.serialization.Serializable
data class Edge(@kotlinx.serialization.SerialName("from") val from: String, val to: String, val reason: String? = null)
@kotlinx.serialization.Serializable
data class DecisionTree(
val id: String,
val user: String = "jeremiah",
val created_at: String,
val goal: String,
val constraints: List<String> = emptyList(),
val facts: Map<String, List<String>> = emptyMap(),
val nodes: List<Node>,
val edges: List<Edge> = emptyList(),
val next_actions: List<String> = emptyList(),
val version: Int = 1
)
interface PiApi {
@POST("/heartbeat")
suspend fun heartbeat(@Body hb: Map<String, String>, @Header("Authorization") auth: String): Map<String, Any>
@POST("/ingest")
suspend fun ingest(@Body tree: DecisionTree, @Header("Authorization") auth: String): Map<String, String>
@POST("/improve")
suspend fun improve(@Body body: Map<String, Any>, @Header("Authorization") auth: String): Map<String, @JvmSuppressWildcards Any>
}
fun encryptedPrefs(context: Context): SharedPreferences {
val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
return EncryptedSharedPreferences.create(
context,
"secure_prefs",
masterKey,
EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
}
fun encryptedPrefs(context: Context): SharedPreferences {
val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
return EncryptedSharedPreferences.create(
context,
"secure_prefs",
masterKey,
EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
}
@Composable
fun NewPlanScreen(vm: PlanViewModel = viewModel()) {
var goal by remember { mutableStateOf("") }
var constraints by remember { mutableStateOf("") } // comma‑sep
Column(Modifier.padding(16.dp)) {
Text("Decision‑Tree Planner", style = MaterialTheme.typography.headlineSmall)
OutlinedTextField(goal, { goal = it }, label = { Text("Goal") })
OutlinedTextField(constraints, { constraints = it }, label = { Text("Constraints (comma‑separated)") })
Button(onClick = { vm.submit(goal, constraints.split(',').map { it.trim() }) }) {
Text("Send to Pi")
}
}
}
class PlanViewModel(app: Application): AndroidViewModel(app) {
private val api: PiApi = /* retrofit build with baseUrl("http://192.168.8.109:8088/") */ TODO()
private val prefs = encryptedPrefs(app)
private val token get() = "Bearer " + (prefs.getString("token", null) ?: "")


fun submit(goal: String, cons: List<String>) = viewModelScope.launch {
val tree = DecisionTree(
id = UUID.randomUUID().toString(),
created_at = Instant.now().toString(),
goal = goal,
constraints = cons,
nodes = listOf(Node("root", "Start toward: $goal", 0.5, emptyList()))
)
api.ingest(tree, token)
}
}
class HeartbeatWorker(app: Context, params: WorkerParameters): CoroutineWorker(app, params) {
override suspend fun doWork(): Result {
val prefs = encryptedPrefs(applicationContext)
val token = "Bearer " + (prefs.getString("token", null) ?: return Result.retry())
val api: PiApi = /* build retrofit */ TODO()
api.heartbeat(mapOf("device_id" to "galaxy-phone", "device_type" to "phone", "app_version" to "0.1.0"), token)
return Result.success()
}
}
class HeartbeatWorker(app: Context, params: WorkerParameters): CoroutineWorker(app, params) {
override suspend fun doWork(): Result {
val prefs = encryptedPrefs(applicationContext)
val token = "Bearer " + (prefs.getString("token", null) ?: return Result.retry())
val api: PiApi = /* build retrofit */ TODO()
api.heartbeat(mapOf("device_id" to "galaxy-phone", "device_type" to "phone", "app_version" to "0.1.0"), token)
return Result.success()
}
}WorkManager.getInstance(context).enqueueUniquePeriodicWork(
"hb", ExistingPeriodicWorkPolicy.UPDATE,
PeriodicWorkRequestBuilder<HeartbeatWorker>(1, TimeUnit.HOURS).build()
)
