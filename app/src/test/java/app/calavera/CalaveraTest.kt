package app.calavera

import app.calavera.data.Backup
import app.calavera.data.Check
import app.calavera.data.Day
import app.calavera.data.Flag
import app.calavera.data.Footprint
import app.calavera.data.Memory
import app.calavera.data.Poem
import app.calavera.data.Settings
import app.calavera.data.Soul
import app.calavera.domain.Fear
import app.calavera.domain.Look
import app.calavera.domain.Picado
import app.calavera.domain.Rules
import app.calavera.domain.Safety
import app.calavera.domain.Wardrobe
import app.calavera.notify.Alarms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalaveraTest {
    @Test
    fun fiestaNights() {
        assertTrue(Rules.fiesta(LocalDate.of(2026, 10, 31)))
        assertTrue(Rules.fiesta(LocalDate.of(2026, 11, 2)))
        assertFalse(Rules.fiesta(LocalDate.of(2026, 11, 3)))
        assertEquals(14, Rules.daysToFiesta(LocalDate.of(2026, 10, 18)))
        assertEquals(0, Rules.daysToFiesta(LocalDate.of(2026, 11, 1)))
        assertEquals(363, Rules.daysToFiesta(LocalDate.of(2026, 11, 3)))
    }

    @Test
    fun ageAndBirthdays() {
        val b = LocalDate.of(1988, 3, 14)
        assertEquals(38, Rules.age(b, LocalDate.of(2026, 10, 18)))
        assertEquals(37, Rules.age(b, LocalDate.of(2026, 3, 13)))
        assertTrue(Rules.birthdayPassed(b, LocalDate.of(2026, 3, 14)))
        assertFalse(Rules.birthdayPassed(b, LocalDate.of(2026, 3, 13)))
        // A leap-day birthday counts from the 28th in ordinary years.
        assertTrue(Rules.birthdayPassed(LocalDate.of(2000, 2, 29), LocalDate.of(2027, 2, 28)))
    }

    @Test
    fun privilegesOfYears() {
        assertEquals(emptyList<String>(), Wardrobe.giftsForAge(29))
        assertEquals(listOf("glasses"), Wardrobe.giftsForAge(30))
        assertEquals(listOf("glasses", "cane"), Wardrobe.giftsForAge(45))
        assertEquals(Wardrobe.elder, Wardrobe.giftsForAge(90))
        assertEquals("cane", Wardrobe.nextGift(Wardrobe.starter + "glasses"))
        // Elder items are never for sale and everything else has a slot of its own.
        assertTrue(Wardrobe.items.filter { it.elder }.all { it.price == 0 })
        assertTrue(Wardrobe.starter.all { Wardrobe.item(it)?.price == 0 })
    }

    @Test
    fun lookSurvivesEncoding() {
        val l = Look(hat = "catrina", paint = "lace", clothes = "vestido", hand = "guitar", extra = "goldtooth", color = 3)
        assertEquals(l, Look.decode(l.encode()))
        // Junk falls back to the defaults, item by item.
        assertEquals(Look().copy(paint = "stars"), Look.decode("bogus;stars;guitar;x;y;99").copy(color = 0))
    }

    @Test
    fun picadoUnfoldsSymmetrically() {
        val p = Picado(Picado.Fold.QUARTER, 2, listOf(listOf(Picado.Pt(0.2f, 0.4f), Picado.Pt(0.6f, 0.8f))))
        val back = Picado.decode(p.encode())
        assertNotNull(back)
        assertEquals(Picado.Fold.QUARTER, back!!.fold)
        val u = back.unfolded()
        assertEquals(4, u.size)
        assertEquals(0.1f, u[0][0].x, 0.001f)
        assertEquals(0.9f, u[1][0].x, 0.001f)
        assertEquals(0.8f, u[2][0].y, 0.001f)
        assertEquals(2, Picado(Picado.Fold.HALF, 0, p.cuts).unfolded().size)
        assertEquals(null, Picado.decode("nonsense"))
    }

    @Test
    fun flagsFadeInAWeek() {
        assertEquals(1f, Rules.flagLife(1000, 1000), 0.001f)
        assertEquals(0.5f, Rules.flagLife(0, Rules.FLAG_LIFE_MS / 2), 0.001f)
        assertEquals(0f, Rules.flagLife(0, Rules.FLAG_LIFE_MS * 2), 0.001f)
    }

    @Test
    fun lotteryDrawsEachCardOnce() {
        val drawn = mutableSetOf<Fear>()
        for (d in 0L until 12L) drawn += Rules.draw(drawn, d)
        assertEquals(Fear.entries.toSet(), drawn)
    }

    @Test
    fun thermometer() {
        assertEquals(0, Rules.score(List(8) { 1 }))
        assertEquals(100, Rules.score(List(8) { 5 }))
        assertEquals(50, Rules.score(List(8) { 3 }))
        assertTrue(Rules.checkDue(null, 0))
        assertFalse(Rules.checkDue(0, 10L * 24 * 3600 * 1000))
    }

    @Test
    fun years() {
        assertEquals("1931 – 2019", Rules.years("1931", "12 May 2019"))
        assertEquals("† 2019", Rules.years("", "2019"))
        assertEquals("", Rules.years("long ago", ""))
    }

    @Test
    fun careNoticesWorryingWords() {
        assertTrue(Safety.worrying("Some days I want to die"))
        assertTrue(Safety.worrying("Иногда хочу умереть"))
        assertTrue(Safety.worrying("не хочу ЖИТЬ"))
        assertFalse(Safety.worrying("Death came for me and I offered her cake"))
        assertFalse(Safety.worrying("Смерть пришла к Марте в среду"))
    }

    @Test
    fun catrinaComesAtHerHour() {
        val now = LocalDateTime.of(2026, 10, 18, 9, 0)
        val at = Alarms.next(600, now, ZoneOffset.UTC)
        assertEquals(LocalDateTime.of(2026, 10, 18, 10, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), at)
        val later = Alarms.next(600, LocalDateTime.of(2026, 10, 18, 10, 0), ZoneOffset.UTC)
        assertEquals(LocalDateTime.of(2026, 10, 19, 10, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), later)
    }

    @Test
    fun backupRoundTrip() {
        val c = Backup.Content(
            souls = listOf(Soul(1, "Nana", "grandmother", "1931", "2019", "a.jpg", "BREAD,COFFEE", false, 5, 10)),
            memories = listOf(Memory(2, 1, "She sang", 20)),
            flags = listOf(Flag(3, Picado.preset(1).encode(), 30)),
            poems = listOf(Poem(4, "La Flaca came…", 40)),
            footprints = listOf(Footprint(5, "Lucía", "bike", 50)),
            checks = listOf(Check(6, 60, "1,2,3,4,5,1,2,3", 40)),
            days = listOf(Day("2026-10-18", true, "TIME", true)),
            settings = Settings(name = "Marta", birth = "1988-03-14", petals = 23, look = Look(hat = "sombrero"), plan = "{\"music\":\"loud\"}"),
        )
        val back = Backup.decode(Backup.encode(c))
        assertEquals(c.souls, back.souls)
        assertEquals(c.memories, back.memories)
        assertEquals(c.flags, back.flags)
        assertEquals(c.poems, back.poems)
        assertEquals(c.footprints, back.footprints)
        assertEquals(c.checks, back.checks)
        assertEquals(c.days, back.days)
        assertEquals("Marta", back.settings.name)
        assertEquals(23, back.settings.petals)
        assertEquals("sombrero", back.settings.look.hat)
        assertEquals("{\"music\":\"loud\"}", back.settings.plan)
    }

    @Test(expected = IllegalArgumentException::class)
    fun notABackup() {
        Backup.decode("{\"app\":\"silverbox\"}")
    }
}
