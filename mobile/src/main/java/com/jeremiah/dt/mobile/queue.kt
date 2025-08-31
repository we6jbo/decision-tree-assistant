package com.jeremiah.dt.mobile
import androidx.room.*

@Entity(tableName = "pending_events")
data class PendingEvent(
  @PrimaryKey val id: String,                 // UUID
  val createdAt: Long,
  val kind: String,                           // "ingest" | "improve" | "heartbeat"
  val payloadJson: String                     // serialized JSON to send
)

@Dao
interface PendingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(e: PendingEvent)
  @Query("SELECT * FROM pending_events ORDER BY createdAt ASC LIMIT 1")
  suspend fun peek(): PendingEvent?
  @Query("DELETE FROM pending_events WHERE id = :id") suspend fun delete(id: String)
  @Query("SELECT COUNT(*) FROM pending_events") suspend fun count(): Int
}

@Database(entities = [PendingEvent::class], version = 1)
abstract class QueueDb : RoomDatabase() { abstract fun dao(): PendingDao }

object Queue {
  @Volatile private var db: QueueDb? = null
  fun get(ctx: android.content.Context): QueueDb =
    db ?: Room.databaseBuilder(ctx, QueueDb::class.java, "queue.db").fallbackToDestructiveMigration().build().also { db = it }
}

