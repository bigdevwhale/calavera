package app.calavera.domain

enum class Slot { HAT, PAINT, CLOTHES, HAND, EXTRA }

/**
 * Something the calaca can wear. [price] is in marigold petals; an [elder] item can't be bought:
 * it is a privilege of years, brought by Catrina on birthdays.
 */
data class Item(val id: String, val slot: Slot, val price: Int = 0, val elder: Boolean = false)

/** What the calaca has on. Ids from [Wardrobe]; [color] indexes [Wardrobe.paints]. */
data class Look(
    val hat: String = "flowers",
    val paint: String = "petals",
    val clothes: String = "poncho",
    val hand: String = "bouquet",
    val extra: String = "none",
    val color: Int = 0,
) {
    fun of(slot: Slot): String = when (slot) {
        Slot.HAT -> hat
        Slot.PAINT -> paint
        Slot.CLOTHES -> clothes
        Slot.HAND -> hand
        Slot.EXTRA -> extra
    }

    fun with(item: Item): Look = when (item.slot) {
        Slot.HAT -> copy(hat = item.id)
        Slot.PAINT -> copy(paint = item.id)
        Slot.CLOTHES -> copy(clothes = item.id)
        Slot.HAND -> copy(hand = item.id)
        Slot.EXTRA -> copy(extra = item.id)
    }

    fun encode(): String = listOf(hat, paint, clothes, hand, extra, color.toString()).joinToString(";")

    companion object {
        fun decode(s: String): Look {
            val p = s.split(';')
            if (p.size < 6) return Look()
            val d = Look()
            fun ok(id: String, slot: Slot, fallback: String) = if (Wardrobe.item(id)?.slot == slot) id else fallback
            return Look(
                hat = ok(p[0], Slot.HAT, d.hat),
                paint = ok(p[1], Slot.PAINT, d.paint),
                clothes = ok(p[2], Slot.CLOTHES, d.clothes),
                hand = ok(p[3], Slot.HAND, d.hand),
                extra = ok(p[4], Slot.EXTRA, d.extra),
                color = (p[5].toIntOrNull() ?: 0).coerceIn(0, Wardrobe.paints - 1),
            )
        }
    }
}

object Wardrobe {
    const val START_PETALS = 10

    /** Paint colours for the skull: fuchsia, turquoise, marigold, lime, violet. */
    const val paints = 5

    val items = listOf(
        Item("none_hat", Slot.HAT),
        Item("flowers", Slot.HAT),
        Item("sombrero", Slot.HAT, 8),
        Item("tophat", Slot.HAT, 12),
        Item("catrina", Slot.HAT, 18),

        Item("petals", Slot.PAINT),
        Item("hearts", Slot.PAINT, 5),
        Item("spirals", Slot.PAINT, 6),
        Item("stars", Slot.PAINT, 8),
        Item("lace", Slot.PAINT, 10),

        Item("bare", Slot.CLOTHES),
        Item("poncho", Slot.CLOTHES),
        Item("guayabera", Slot.CLOTHES, 8),
        Item("charro", Slot.CLOTHES, 15),
        Item("vestido", Slot.CLOTHES, 15),

        Item("none_hand", Slot.HAND),
        Item("bouquet", Slot.HAND),
        Item("maracas", Slot.HAND, 6),
        Item("guitar", Slot.HAND, 12),
        Item("cane", Slot.HAND, elder = true),

        Item("none", Slot.EXTRA),
        Item("collar", Slot.EXTRA, 5),
        Item("mustache", Slot.EXTRA, 6),
        Item("glasses", Slot.EXTRA, elder = true),
        Item("rebozo", Slot.EXTRA, elder = true),
        Item("goldtooth", Slot.EXTRA, elder = true),
    )

    val starter: Set<String> = items.filter { it.price == 0 && !it.elder }.map { it.id }.toSet()

    /** The order Catrina hands out the privileges of years. */
    val elder: List<String> = listOf("glasses", "cane", "rebozo", "goldtooth")

    fun item(id: String): Item? = items.firstOrNull { it.id == id }
    fun of(slot: Slot): List<Item> = items.filter { it.slot == slot }

    /** The next privilege not yet given, or null when all have been. */
    fun nextGift(owned: Set<String>): String? = elder.firstOrNull { it !in owned }

    /** Privileges someone already has by their years when they arrive: one at thirty and one more each decade. */
    fun giftsForAge(age: Int?): List<String> =
        if (age == null || age < 30) emptyList() else elder.take((age - 30) / 10 + 1)
}
