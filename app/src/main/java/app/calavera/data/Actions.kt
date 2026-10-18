package app.calavera.data

import app.calavera.AppContainer
import app.calavera.domain.Item
import app.calavera.domain.Reward
import app.calavera.domain.Rules
import app.calavera.domain.Wardrobe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Petals just earned, for the little "+3" that floats up. */
object Petals {
    private val _events = MutableSharedFlow<Int>(extraBufferCapacity = 4)
    val events: SharedFlow<Int> = _events.asSharedFlow()
    fun tell(n: Int) { if (n > 0) _events.tryEmit(n) }
}

/** Pays a reward once a day per kind; returns the petals given (0 when already paid today). */
suspend fun AppContainer.earn(r: Reward): Int {
    val today = Rules.today().toString()
    var gained = 0
    settings.update { s ->
        val earned = if (s.earnedDay == today) s.earned else emptySet()
        if (r.name in earned) {
            gained = 0
            s.copy(earnedDay = today, earned = earned)
        } else {
            gained = r.petals
            s.copy(earnedDay = today, earned = earned + r.name, petals = s.petals + r.petals)
        }
    }
    Petals.tell(gained)
    return gained
}

/** Buys and puts on an item; false when there aren't petals enough. */
suspend fun AppContainer.buy(item: Item): Boolean {
    var ok = false
    settings.update { s ->
        when {
            item.id in s.owned -> { ok = true; s.copy(look = s.look.with(item)) }
            item.elder || s.petals < item.price -> { ok = false; s }
            else -> { ok = true; s.copy(petals = s.petals - item.price, owned = s.owned + item.id, look = s.look.with(item)) }
        }
    }
    return ok
}

/**
 * On or after the birthday, once a year, Catrina brings the next privilege of years and 10 petals.
 * Returns the privilege's id, "" when all have been given already, or null when nothing was due.
 */
suspend fun AppContainer.birthdayGift(): String? {
    val s = settings.now()
    val birth = Rules.birth(s.birth) ?: return null
    val today = Rules.today()
    if (!s.onboarded || s.giftYear >= today.year || !Rules.birthdayPassed(birth, today)) return null
    var gift: String? = null
    settings.update { cur ->
        if (cur.giftYear >= today.year) return@update cur
        gift = Wardrobe.nextGift(cur.owned) ?: ""
        cur.copy(giftYear = today.year, owned = cur.owned + listOfNotNull(gift?.takeIf { it.isNotEmpty() }), petals = cur.petals + 10)
    }
    return gift
}

/** Today's row with Catrina, created on first touch. */
suspend fun AppContainer.today(): Day {
    val key = Rules.today().toString()
    return db.dao().day(key) ?: Day(key).also { db.dao().put(it) }
}
