package app.calavera.domain

import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit

/** What petals are earned for. Each kind pays once a day. */
enum class Reward(val petals: Int) {
    ERRAND(3),
    CANDLE(1),
    CARD(3),
    POEM(5),
    FLAG(3),
    FOOTPRINT(2),
    CHECK(5),
    MEMORY(2),
    QUESTION(2),
}

/** Things to set on the ofrenda for someone. */
enum class Offering { BREAD, COFFEE, FRUIT, SUGAR_SKULL, CHOCOLATE, TEQUILA, BOOK, MUSIC, CIGAR, TOY, SWEETS, WATER }

/** The fears on the lotería cards, numbered as on the deck. */
enum class Fear { FORGOTTEN, NOTHING, PAIN, TIME, HAND, MIRROR, FAREWELL, UNFINISHED, ALONE, REGRET, UNKNOWN, BODY }

object Rules {
    fun today(): LocalDate = LocalDate.now()

    fun birth(iso: String): LocalDate? = runCatching { LocalDate.parse(iso) }.getOrNull()

    fun age(birth: LocalDate?, today: LocalDate = today()): Int? =
        birth?.let { ChronoUnit.YEARS.between(it, today).toInt() }?.takeIf { it in 0..130 }

    /** The birthday this year has come (today or earlier this year). */
    fun birthdayPassed(birth: LocalDate?, today: LocalDate = today()): Boolean {
        if (birth == null) return false
        val md = MonthDay.from(birth)
        val thisYear = if (md == MonthDay.of(2, 29) && !today.isLeapYear) LocalDate.of(today.year, 2, 28) else md.atYear(today.year)
        return !today.isBefore(thisYear)
    }

    fun isBirthday(birth: LocalDate?, today: LocalDate = today()): Boolean =
        birth != null && birth.monthValue == today.monthValue && birth.dayOfMonth == today.dayOfMonth

    /** The nights of the dead: the 31st of October to the 2nd of November. */
    fun fiesta(today: LocalDate = today()): Boolean =
        (today.monthValue == 10 && today.dayOfMonth == 31) || (today.monthValue == 11 && today.dayOfMonth <= 2)

    /** Days until the 1st of November (0 during the fiesta). */
    fun daysToFiesta(today: LocalDate = today()): Int {
        if (fiesta(today)) return 0
        var next = LocalDate.of(today.year, 11, 1)
        if (today.isAfter(next)) next = next.plusYears(1)
        return ChronoUnit.DAYS.between(today, next).toInt()
    }

    /** Catrina's word for the day, the same all day, cycling through [count]. */
    fun wordOfDay(today: LocalDate, count: Int): Int = Math.floorMod(today.toEpochDay(), count.toLong()).toInt()

    /** The card drawn today from those not yet drawn; all drawn → any but yesterday's. */
    fun draw(drawn: Set<Fear>, seed: Long): Fear {
        val left = Fear.entries.filter { it !in drawn }.ifEmpty { Fear.entries }
        return left[Math.floorMod(seed * 31 + 7, left.size.toLong()).toInt()]
    }

    /** A papel picado hangs a week. */
    const val FLAG_LIFE_MS = 7L * 24 * 3600 * 1000

    /** 1 when freshly hung, falling to 0 as the week ends. */
    fun flagLife(hungAt: Long, now: Long): Float = (1f - (now - hungAt).toFloat() / FLAG_LIFE_MS).coerceIn(0f, 1f)

    /** A candle burns for the rest of the day it was lit. */
    fun candleLit(candleAt: Long, today: LocalDate = today(), zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): Boolean =
        candleAt > 0 && java.time.Instant.ofEpochMilli(candleAt).atZone(zone).toLocalDate() == today

    /** Fear thermometer: answers 1..5 → 0..100. */
    fun score(answers: List<Int>): Int {
        if (answers.isEmpty()) return 0
        val sum = answers.sumOf { it.coerceIn(1, 5) - 1 }
        return Math.round(sum * 100f / (answers.size * 4))
    }

    /** A month since the last reading (or none yet). */
    fun checkDue(lastAt: Long?, now: Long): Boolean = lastAt == null || now - lastAt > 28L * 24 * 3600 * 1000

    /** Years between two free-text dates, if both have a year in them. */
    fun years(born: String, died: String): String {
        val a = Regex("\\d{4}").findAll(born).lastOrNull()?.value
        val b = Regex("\\d{4}").findAll(died).lastOrNull()?.value
        return when {
            a != null && b != null -> "$a – $b"
            b != null -> "† $b"
            a != null -> "$a –"
            else -> ""
        }
    }
}
