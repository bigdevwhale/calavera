package app.calavera.data

import android.content.Context
import android.net.Uri
import app.calavera.domain.Look
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Everything in one zip: a JSON index of the ofrenda, the verses, the flags, the road and the
 * readings, the calaca's wardrobe and the last fiesta, plus the portraits.
 */
object Backup {
    private const val VERSION = 1
    private const val INDEX = "calavera.json"

    class Content(
        val souls: List<Soul>,
        val memories: List<Memory>,
        val flags: List<Flag>,
        val poems: List<Poem>,
        val footprints: List<Footprint>,
        val checks: List<Check>,
        val days: List<Day>,
        val settings: Settings,
    )

    fun encode(c: Content): String = JSONObject()
        .put("app", "calavera")
        .put("version", VERSION)
        .put("souls", JSONArray(c.souls.map {
            JSONObject().put("id", it.id).put("name", it.name).put("relation", it.relation).put("born", it.born).put("died", it.died)
                .put("photo", it.photo).put("offerings", it.offerings).put("pet", it.pet).put("candleAt", it.candleAt).put("createdAt", it.createdAt)
        }))
        .put("memories", JSONArray(c.memories.map { JSONObject().put("id", it.id).put("soulId", it.soulId).put("text", it.text).put("at", it.at) }))
        .put("flags", JSONArray(c.flags.map { JSONObject().put("id", it.id).put("design", it.design).put("hungAt", it.hungAt) }))
        .put("poems", JSONArray(c.poems.map { JSONObject().put("id", it.id).put("text", it.text).put("at", it.at) }))
        .put("footprints", JSONArray(c.footprints.map { JSONObject().put("id", it.id).put("who", it.who).put("what", it.what).put("at", it.at) }))
        .put("checks", JSONArray(c.checks.map { JSONObject().put("id", it.id).put("at", it.at).put("answers", it.answers).put("score", it.score) }))
        .put("days", JSONArray(c.days.map { JSONObject().put("day", it.day).put("errandDone", it.errandDone).put("card", it.card).put("cardDone", it.cardDone) }))
        .put("settings", c.settings.let {
            JSONObject().put("name", it.name).put("birth", it.birth).put("petals", it.petals).put("owned", JSONArray(it.owned.toList()))
                .put("look", it.look.encode()).put("giftYear", it.giftYear).put("notify", it.notify).put("notifyAt", it.notifyAt).put("plan", it.plan)
        })
        .toString(2)

    /** Throws [IllegalArgumentException] when the text is not a Calavera index. */
    fun decode(text: String): Content {
        val root = runCatching { JSONObject(text) }.getOrNull()
        require(root != null && root.optString("app") == "calavera") { "Not a Calavera backup" }
        fun list(key: String) = root.optJSONArray(key) ?: JSONArray()
        fun <T> JSONArray.each(f: JSONObject.() -> T): List<T> = (0 until length()).map { getJSONObject(it).f() }
        val s = root.optJSONObject("settings") ?: JSONObject()
        val owned = s.optJSONArray("owned")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
        return Content(
            souls = list("souls").each {
                Soul(
                    id = getLong("id"), name = getString("name"), relation = optString("relation"), born = optString("born"), died = optString("died"),
                    photo = optString("photo"), offerings = optString("offerings"), pet = optBoolean("pet"), candleAt = optLong("candleAt"), createdAt = getLong("createdAt"),
                )
            },
            memories = list("memories").each { Memory(getLong("id"), getLong("soulId"), getString("text"), getLong("at")) },
            flags = list("flags").each { Flag(getLong("id"), getString("design"), getLong("hungAt")) },
            poems = list("poems").each { Poem(getLong("id"), getString("text"), getLong("at")) },
            footprints = list("footprints").each { Footprint(getLong("id"), getString("who"), optString("what"), getLong("at")) },
            checks = list("checks").each { Check(getLong("id"), getLong("at"), getString("answers"), getInt("score")) },
            days = list("days").each { Day(getString("day"), optBoolean("errandDone"), optString("card"), optBoolean("cardDone")) },
            settings = Settings(
                onboarded = true,
                name = s.optString("name"),
                birth = s.optString("birth"),
                petals = s.optInt("petals", 0),
                owned = (owned ?: emptySet()) + app.calavera.domain.Wardrobe.starter,
                look = Look.decode(s.optString("look")),
                giftYear = s.optInt("giftYear"),
                notify = s.optBoolean("notify", true),
                notifyAt = s.optInt("notifyAt", 600),
                plan = s.optString("plan"),
            ),
        )
    }

    suspend fun collect(db: CalaveraDatabase, settings: Settings): Content = db.dao().run {
        Content(souls(), memories(), flags(), poems(), footprints(), checks(), days(), settings)
    }

    fun write(context: Context, uri: Uri, c: Content) {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            ZipOutputStream(os.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(INDEX))
                zip.write(encode(c).toByteArray())
                zip.closeEntry()
                c.souls.map { it.photo }.filter { it.isNotEmpty() }.distinct().forEach { name ->
                    val file = Photos.file(context, name)
                    if (!file.exists()) return@forEach
                    zip.putNextEntry(ZipEntry("photos/$name"))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        } ?: error("Can't write there")
    }

    /** Reads a backup into a fresh folder first, so a broken file never wipes what is here. */
    fun read(context: Context, uri: Uri): Pair<Content, File> {
        val tmp = File(context.cacheDir, "restore").apply { deleteRecursively(); mkdirs() }
        var index: String? = null
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    when {
                        e.name == INDEX -> index = zip.readBytes().decodeToString()
                        e.name.startsWith("photos/") && !e.isDirectory -> {
                            val name = e.name.removePrefix("photos/")
                            require(!name.contains('/') && !name.contains("..")) { "Bad entry" }
                            File(tmp, name).outputStream().use { zip.copyTo(it) }
                        }
                    }
                }
            }
        } ?: error("Can't read that")
        return decode(index ?: throw IllegalArgumentException("Not a Calavera backup")) to tmp
    }

    /** Swaps everything for the backup's contents. */
    suspend fun restore(context: Context, db: CalaveraDatabase, store: SettingsStore, c: Content, photos: File) {
        val dir = Photos.dir(context)
        dir.listFiles()?.forEach { it.delete() }
        photos.listFiles()?.forEach { it.copyTo(File(dir, it.name), overwrite = true) }
        photos.deleteRecursively()
        db.dao().replaceAll(c)
        store.update { cur -> c.settings.copy(seen = cur.seen) }
    }
}
