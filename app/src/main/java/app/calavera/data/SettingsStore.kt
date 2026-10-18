package app.calavera.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.calavera.domain.Look
import app.calavera.domain.Wardrobe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    val name: String = "",
    /** ISO birth date, or empty. */
    val birth: String = "",
    /** Marigold petals: earned by practices, spent in the wardrobe. */
    val petals: Int = Wardrobe.START_PETALS,
    /** The day [earned] belongs to; rewards are once a day per kind. */
    val earnedDay: String = "",
    val earned: Set<String> = emptySet(),
    /** Wardrobe items bought or given. */
    val owned: Set<String> = Wardrobe.starter,
    val look: Look = Look(),
    /** The last year Catrina brought a birthday present. */
    val giftYear: Int = 0,
    /** Catrina's morning visit as a notice. */
    val notify: Boolean = true,
    /** Minutes after midnight. */
    val notifyAt: Int = 10 * 60,
    /** The last fiesta, as JSON of section → text. */
    val plan: String = "",
    /** Hints already shown. */
    val seen: Set<String> = emptySet(),
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        name = this[NAME] ?: "",
        birth = this[BIRTH] ?: "",
        petals = this[PETALS] ?: Wardrobe.START_PETALS,
        earnedDay = this[EARNED_DAY] ?: "",
        earned = this[EARNED] ?: emptySet(),
        owned = this[OWNED] ?: Wardrobe.starter,
        look = Look.decode(this[LOOK] ?: ""),
        giftYear = this[GIFT_YEAR] ?: 0,
        notify = this[NOTIFY] ?: true,
        notifyAt = this[NOTIFY_AT] ?: (10 * 60),
        plan = this[PLAN] ?: "",
        seen = this[SEEN] ?: emptySet(),
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[NAME] = s.name
            it[BIRTH] = s.birth
            it[PETALS] = s.petals
            it[EARNED_DAY] = s.earnedDay
            it[EARNED] = s.earned
            it[OWNED] = s.owned
            it[LOOK] = s.look.encode()
            it[GIFT_YEAR] = s.giftYear
            it[NOTIFY] = s.notify
            it[NOTIFY_AT] = s.notifyAt
            it[PLAN] = s.plan
            it[SEEN] = s.seen
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val NAME = stringPreferencesKey("name")
        val BIRTH = stringPreferencesKey("birth")
        val PETALS = intPreferencesKey("petals")
        val EARNED_DAY = stringPreferencesKey("earned_day")
        val EARNED = stringSetPreferencesKey("earned")
        val OWNED = stringSetPreferencesKey("owned")
        val LOOK = stringPreferencesKey("look")
        val GIFT_YEAR = intPreferencesKey("gift_year")
        val NOTIFY = booleanPreferencesKey("notify")
        val NOTIFY_AT = intPreferencesKey("notify_at")
        val PLAN = stringPreferencesKey("plan")
        val SEEN = stringSetPreferencesKey("seen")
    }
}
