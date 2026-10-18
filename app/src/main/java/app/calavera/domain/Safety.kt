package app.calavera.domain

/**
 * Calavera is about making peace with death, not wishing for it. When something written sounds like
 * someone wants to die now, the app gently points to people who can help.
 */
object Safety {
    private val phrases = listOf(
        // English
        "kill myself", "killing myself", "suicide", "suicidal", "end my life", "end it all", "want to die",
        "wanna die", "don't want to live", "dont want to live", "no reason to live", "better off dead", "hurt myself",
        // Russian
        "покончить с собой", "покончу с собой", "суицид", "самоубий", "хочу умереть", "не хочу жить", "убить себя",
        "убью себя", "нет смысла жить", "лучше бы я умер", "лучше бы меня не было", "жить не хочется", "навредить себе",
    )

    fun worrying(text: String): Boolean {
        val t = text.lowercase().replace('ё', 'е')
        return phrases.any { t.contains(it) }
    }

    /** An international directory of free, confidential helplines. */
    const val HELP_URL = "https://findahelpline.com"
}
