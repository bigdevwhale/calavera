package app.calavera.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Someone on the ofrenda: a person or a pet who is no longer here. */
@Entity(tableName = "souls")
data class Soul(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** "My grandmother", "our cat". */
    val relation: String = "",
    /** Free text such as "1931" or "12 May 1931"; years are what matter. */
    val born: String = "",
    val died: String = "",
    /** A file in the photo folder, or empty for a drawn frame. */
    val photo: String = "",
    /** [app.calavera.domain.Offering] names, comma separated. */
    val offerings: String = "",
    val pet: Boolean = false,
    /** When a candle was last lit for them. */
    val candleAt: Long = 0,
    val createdAt: Long,
) {
    val offeringList: List<String> get() = offerings.split(',').filter { it.isNotBlank() }
}

/** A memory told about a soul. */
@Entity(tableName = "memories", indices = [Index("soulId")])
data class Memory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val soulId: Long,
    val text: String,
    val at: Long,
)

/** A cut papel picado flag. It hangs on the garland for a week and then the wind takes it. */
@Entity(tableName = "flags")
data class Flag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** [app.calavera.domain.Picado] encoding of the fold, colour and cuts. */
    val design: String,
    val hungAt: Long,
)

/** A calaverita: a merry verse about one's own death. */
@Entity(tableName = "poems")
data class Poem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val at: Long,
)

/** A marigold on the road: someone or something that will carry a trace of you. */
@Entity(tableName = "footprints")
data class Footprint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val who: String,
    val what: String = "",
    val at: Long,
)

/** A reading of the fear thermometer. */
@Entity(tableName = "checks")
data class Check(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long,
    /** Answers 1..5, comma separated, in the order of the statements. */
    val answers: String,
    /** 0..100: how hot the fear runs. */
    val score: Int,
)

/** A day with Catrina: whether her errand was done and which lotería card was drawn. */
@Entity(tableName = "days")
data class Day(
    /** ISO date. */
    @PrimaryKey val day: String,
    val errandDone: Boolean = false,
    /** [app.calavera.domain.Fear] name, or empty when no card was drawn. */
    val card: String = "",
    val cardDone: Boolean = false,
)

@Dao
interface CalaveraDao {
    @Query("SELECT * FROM souls ORDER BY createdAt") fun observeSouls(): Flow<List<Soul>>
    @Query("SELECT * FROM souls ORDER BY createdAt") suspend fun souls(): List<Soul>
    @Insert suspend fun insert(s: Soul): Long
    @Update suspend fun update(s: Soul)
    @Query("DELETE FROM souls WHERE id = :id") suspend fun deleteSoul(id: Long)

    @Query("SELECT * FROM memories ORDER BY at DESC") fun observeMemories(): Flow<List<Memory>>
    @Query("SELECT * FROM memories") suspend fun memories(): List<Memory>
    @Insert suspend fun insert(m: Memory): Long
    @Query("DELETE FROM memories WHERE id = :id") suspend fun deleteMemory(id: Long)
    @Query("DELETE FROM memories WHERE soulId = :soulId") suspend fun deleteMemoriesOf(soulId: Long)

    @Query("SELECT * FROM flags ORDER BY hungAt DESC") fun observeFlags(): Flow<List<Flag>>
    @Query("SELECT * FROM flags") suspend fun flags(): List<Flag>
    @Insert suspend fun insert(f: Flag): Long

    @Query("SELECT * FROM poems ORDER BY at DESC") fun observePoems(): Flow<List<Poem>>
    @Query("SELECT * FROM poems") suspend fun poems(): List<Poem>
    @Insert suspend fun insert(p: Poem): Long
    @Update suspend fun update(p: Poem)
    @Query("DELETE FROM poems WHERE id = :id") suspend fun deletePoem(id: Long)

    @Query("SELECT * FROM footprints ORDER BY at") fun observeFootprints(): Flow<List<Footprint>>
    @Query("SELECT * FROM footprints") suspend fun footprints(): List<Footprint>
    @Insert suspend fun insert(f: Footprint): Long
    @Query("DELETE FROM footprints WHERE id = :id") suspend fun deleteFootprint(id: Long)

    @Query("SELECT * FROM checks ORDER BY at") fun observeChecks(): Flow<List<Check>>
    @Query("SELECT * FROM checks") suspend fun checks(): List<Check>
    @Insert suspend fun insert(c: Check): Long

    @Query("SELECT * FROM days ORDER BY day") fun observeDays(): Flow<List<Day>>
    @Query("SELECT * FROM days") suspend fun days(): List<Day>
    @Query("SELECT * FROM days WHERE day = :day") suspend fun day(day: String): Day?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(d: Day)

    @Query("DELETE FROM souls") suspend fun clearSouls()
    @Query("DELETE FROM memories") suspend fun clearMemories()
    @Query("DELETE FROM flags") suspend fun clearFlags()
    @Query("DELETE FROM poems") suspend fun clearPoems()
    @Query("DELETE FROM footprints") suspend fun clearFootprints()
    @Query("DELETE FROM checks") suspend fun clearChecks()
    @Query("DELETE FROM days") suspend fun clearDays()
    @Insert suspend fun insertSouls(l: List<Soul>)
    @Insert suspend fun insertMemories(l: List<Memory>)
    @Insert suspend fun insertFlags(l: List<Flag>)
    @Insert suspend fun insertPoems(l: List<Poem>)
    @Insert suspend fun insertFootprints(l: List<Footprint>)
    @Insert suspend fun insertChecks(l: List<Check>)
    @Insert suspend fun insertDays(l: List<Day>)

    @Transaction
    suspend fun replaceAll(b: Backup.Content) {
        clearSouls(); clearMemories(); clearFlags(); clearPoems(); clearFootprints(); clearChecks(); clearDays()
        insertSouls(b.souls); insertMemories(b.memories); insertFlags(b.flags); insertPoems(b.poems)
        insertFootprints(b.footprints); insertChecks(b.checks); insertDays(b.days)
    }
}

@Database(entities = [Soul::class, Memory::class, Flag::class, Poem::class, Footprint::class, Check::class, Day::class], version = 1, exportSchema = true)
abstract class CalaveraDatabase : RoomDatabase() {
    abstract fun dao(): CalaveraDao

    companion object {
        fun create(context: Context): CalaveraDatabase =
            Room.databaseBuilder(context.applicationContext, CalaveraDatabase::class.java, "calavera.db").build()
    }
}
