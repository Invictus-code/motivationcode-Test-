package de.motivationcode.app.data.quotes

import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.model.Quote
import kotlin.random.Random

fun filterQuotes(quotes: List<Quote>, filter: FeedFilter, favoriteIds: Set<String>): List<Quote> =
    when (filter) {
        FeedFilter.All -> quotes
        FeedFilter.Favorites -> quotes.filter { it.id in favoriteIds }
        FeedFilter.Own -> quotes.filter { it.isOwn }
        is FeedFilter.ByCategory -> quotes.filter { filter.category.id in it.categories }
    }

/**
 * Reihenfolge für den Feed: gemischt mit festem [seed], damit sich die Reihenfolge nicht
 * ändert, wenn man z. B. ein Herz setzt. Favoriten bleiben in fester Reihenfolge.
 * Jedes Zitat bekommt seinen Platz aus Seed + ID; kommt ein eigenes Zitat dazu, wird es
 * nur eingefügt, die übrigen behalten ihre Reihenfolge.
 */
fun buildFeed(
    quotes: List<Quote>,
    filter: FeedFilter,
    favoriteIds: Set<String>,
    seed: Long,
): List<Quote> {
    val filtered = filterQuotes(quotes, filter, favoriteIds)
    return if (filter == FeedFilter.Favorites) filtered else filtered.sortedBy { shuffleKey(seed, it.id) }
}

/** SplitMix64: verteilt Seed + ID gleichmäßig, damit die Reihenfolge zufällig wirkt. */
private fun shuffleKey(seed: Long, id: String): Long {
    var z = seed + id.hashCode() * -0x61c8864680b583ebL
    z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
    z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
    return z xor (z ushr 31)
}

/** Ein zufälliges Zitat für Widget und Erinnerungen; fällt auf alle Zitate zurück, wenn der Filter leer ist. */
fun pickQuote(
    quotes: List<Quote>,
    filter: FeedFilter,
    favoriteIds: Set<String>,
    random: Random = Random.Default,
): Quote? = filterQuotes(quotes, filter, favoriteIds).ifEmpty { quotes }.randomOrNull(random)
