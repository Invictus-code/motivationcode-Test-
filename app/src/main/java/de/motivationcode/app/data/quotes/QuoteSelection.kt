package de.motivationcode.app.data.quotes

import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.model.Quote
import kotlin.random.Random

fun filterQuotes(quotes: List<Quote>, filter: FeedFilter, favoriteIds: Set<String>): List<Quote> =
    when (filter) {
        FeedFilter.All -> quotes
        FeedFilter.Favorites -> quotes.filter { it.id in favoriteIds }
        is FeedFilter.ByCategory -> quotes.filter { filter.category.id in it.categories }
    }

/**
 * Reihenfolge für den Feed: gemischt mit festem [seed], damit sich die Reihenfolge nicht
 * ändert, wenn man z. B. ein Herz setzt. Favoriten bleiben in fester Reihenfolge.
 */
fun buildFeed(
    quotes: List<Quote>,
    filter: FeedFilter,
    favoriteIds: Set<String>,
    seed: Long,
): List<Quote> {
    val filtered = filterQuotes(quotes, filter, favoriteIds)
    return if (filter == FeedFilter.Favorites) filtered else filtered.shuffled(Random(seed))
}

/** Ein zufälliges Zitat für Widget und Erinnerungen; fällt auf alle Zitate zurück, wenn der Filter leer ist. */
fun pickQuote(
    quotes: List<Quote>,
    filter: FeedFilter,
    favoriteIds: Set<String>,
    random: Random = Random.Default,
): Quote? = filterQuotes(quotes, filter, favoriteIds).ifEmpty { quotes }.randomOrNull(random)
