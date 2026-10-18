package app.calavera.data

import app.calavera.AppContainer
import app.calavera.domain.Fear
import app.calavera.domain.Look
import app.calavera.domain.Picado
import app.calavera.domain.Picado.Pt
import app.calavera.domain.Rules
import org.json.JSONObject
import java.util.Locale

/**
 * A made-up person for the demo build (`-Pdemo`): Marta, 38, with three souls on her ofrenda, two
 * calaveritas, flags on the garland and a month of practice behind her. Russian phones get Russian.
 */
object DemoSeed {
    suspend fun seedIfEmpty(c: AppContainer, ru: Boolean = Locale.getDefault().language == "ru") {
        val dao = c.db.dao()
        if (dao.souls().isNotEmpty() || c.settings.now().onboarded) return
        val now = System.currentTimeMillis()
        val day = 24L * 3600 * 1000
        val today = Rules.today()
        fun t(en: String, r: String) = if (ru) r else en

        val nana = dao.insert(
            Soul(
                name = t("Nana Rosa", "Бабушка Нина"), relation = t("my grandmother", "моя бабушка"), born = "1931", died = "2019",
                offerings = "BREAD,COFFEE,MUSIC,SWEETS", createdAt = now - 40 * day, candleAt = now - 3600_000,
            )
        )
        val grandpa = dao.insert(
            Soul(
                name = t("Grandpa Tomás", "Дедушка Коля"), relation = t("my grandfather", "мой дедушка"), born = "1928", died = "2015",
                offerings = "CIGAR,TEQUILA,BOOK", createdAt = now - 39 * day,
            )
        )
        val cat = dao.insert(
            Soul(
                name = t("Pirata", "Барсик"), relation = t("our cat", "наш кот"), born = "2008", died = "2022",
                offerings = "TOY,WATER,FRUIT", pet = true, createdAt = now - 30 * day,
            )
        )
        listOf(
            Memory(soulId = nana, text = t("She sang while frying churros and never once burned one.", "Пела, когда жарила блины, и ни разу ни один не подгорел."), at = now - 20 * day),
            Memory(soulId = nana, text = t("Her rule: if you're sad, first eat, then cry.", "Её правило: если грустно — сначала поешь, потом плачь."), at = now - 6 * day),
            Memory(soulId = grandpa, text = t("He taught me to play dominoes and to lose without sulking.", "Научил меня играть в домино и проигрывать без обид."), at = now - 12 * day),
            Memory(soulId = cat, text = t("Slept on every jigsaw puzzle we ever started.", "Спал на каждом пазле, который мы начинали собирать."), at = now - 9 * day),
        ).forEach { dao.insert(it) }

        dao.insert(
            Poem(
                text = t(
                    "La Flaca came for Marta, late,\nand found her busy with a plate.\n\"Sit down,\" said Marta, \"have a bite!\"\nNow Death drops round on Friday night.",
                    "Смерть пришла к Марте в среду,\nВидит — стол накрыт к обеду.\n«Сядь, — ей молвят, — не спеши,\nПирожка-то откуси!»\nСмерть наелась, подобрела\nИ забыла, что хотела.",
                ),
                at = now - 15 * day,
            )
        )
        dao.insert(
            Poem(
                text = t(
                    "Here lies Marta, who was sure\nshe'd find the time to clean the floor.\nShe never did. The dust remains.\nShe danced instead. No one complains.",
                    "Здесь лежит Марта. Всю жизнь собиралась\nНавести порядок — и не собралась.\nПыль так и осталась лежать на полу.\nЗато танцевала. Поставьте свечу.",
                ),
                at = now - 2 * day,
            )
        )

        val star = (0 until 6).map { k ->
            val a = Math.PI * k / 5 + Math.PI / 2
            listOf(Pt(0.8f, 0.5f), Pt((0.8 + 0.28 * Math.cos(a)).toFloat().coerceIn(0f, 1f), (0.5 + 0.28 * Math.sin(a)).toFloat()))
        }
        val flags = listOf(
            Picado(Picado.Fold.QUARTER, 0, listOf(listOf(Pt(0.55f, 0.55f), Pt(0.95f, 0.95f)), listOf(Pt(0.2f, 0.2f), Pt(0.35f, 0.3f)), listOf(Pt(0.6f, 0.2f), Pt(0.9f, 0.2f)), listOf(Pt(0.2f, 0.7f), Pt(0.25f, 0.9f)))),
            Picado(Picado.Fold.HALF, 1, star + listOf(listOf(Pt(0.15f, 0.15f), Pt(0.4f, 0.15f)), listOf(Pt(0.2f, 0.85f), Pt(0.5f, 0.85f)))),
            Picado(Picado.Fold.QUARTER, 2, listOf(listOf(Pt(0.3f, 0.3f), Pt(0.8f, 0.8f)), listOf(Pt(0.8f, 0.3f), Pt(0.3f, 0.8f)), listOf(Pt(0.95f, 0.5f), Pt(0.75f, 0.5f)))),
            Picado.preset(3), Picado.preset(4),
        )
        flags.forEachIndexed { i, p -> dao.insert(Flag(design = p.encode(), hungAt = now - (i * 1.3 * day).toLong() - 3600_000)) }

        listOf(
            t("My niece Lucía", "Племянница Соня") to t("taught her to ride a bike", "научила её кататься на велосипеде"),
            t("The lemon tree", "Лимонное дерево") to t("planted it in 2019; it fruits now", "посадила в 2019-м, теперь плодоносит"),
            t("Andrés", "Андрей") to t("talked him into applying for that job", "уговорила его подать на ту работу"),
            t("Neighbour's kids", "Соседские дети") to t("my abuela's soup recipe", "бабушкин рецепт супа"),
            t("The book club", "Книжный клуб") to t("started it, nine years now", "основала его девять лет назад"),
            t("Mum", "Мама") to t("called every Sunday", "звонила каждое воскресенье"),
        ).forEachIndexed { i, (who, what) -> dao.insert(Footprint(who = who, what = what, at = now - (30 - i * 4) * day)) }

        listOf(68 to 62L, 55 to 33L, 43 to 3L).forEach { (score, ago) ->
            val a = List(8) { ((score / 25) + 1 + (it % 2)).coerceIn(1, 5) }
            dao.insert(Check(at = now - ago * day, answers = a.joinToString(","), score = score))
        }

        val cards = listOf(Fear.TIME, Fear.MIRROR, Fear.FORGOTTEN, Fear.FAREWELL, Fear.UNFINISHED, Fear.PAIN, Fear.HAND)
        cards.forEachIndexed { i, f -> dao.put(Day(today.minusDays((cards.size - i).toLong()).toString(), errandDone = i % 3 != 1, card = f.name, cardDone = true)) }

        val plan = JSONObject()
            .put("music", t("Chavela Vargas, then something loud to dance to", "Сначала Высоцкий, потом что-нибудь громкое, чтобы танцевать"))
            .put("food", t("Nana's tamales. Lots. Nobody leaves hungry.", "Бабушкины пироги. Много. Чтобы никто не ушёл голодным."))
            .put("remember", t("That I laughed too loud and always brought dessert.", "Что я смеялась слишком громко и всегда приносила десерт."))
            .toString()

        c.settings.update {
            it.copy(
                onboarded = true,
                name = "Marta".let { n -> if (ru) "Марта" else n },
                birth = today.minusYears(38).minusDays(40).toString(),
                petals = 23,
                owned = it.owned + setOf("sombrero", "catrina", "hearts", "vestido", "guitar", "glasses", "collar"),
                look = Look(hat = "catrina", paint = "hearts", clothes = "vestido", hand = "bouquet", extra = "glasses", color = 0),
                giftYear = today.year,
                plan = plan,
                notify = false,
                seen = setOf("tip:casa", "tip:ofrenda", "tip:fiesta"),
            )
        }
    }
}
