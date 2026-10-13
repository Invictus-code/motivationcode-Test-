package de.motivationcode.app.data

import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.quotes.buildFeed
import de.motivationcode.app.data.quotes.filterQuotes
import de.motivationcode.app.data.quotes.pickQuote
import de.motivationcode.app.testing.sampleOwnQuote
import de.motivationcode.app.testing.sampleQuotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuoteSelectionTest {

    @Test
    fun `category filter keeps only matching quotes`() {
        val result = filterQuotes(sampleQuotes, FeedFilter.ByCategory(Category.MUT), emptySet())
        assertEquals(listOf("a", "c"), result.map { it.id })
    }

    @Test
    fun `favorites filter keeps only favorites`() {
        val result = filterQuotes(sampleQuotes, FeedFilter.Favorites, setOf("e", "b"))
        assertEquals(listOf("b", "e"), result.map { it.id })
    }

    @Test
    fun `feed order is stable for the same seed`() {
        val first = buildFeed(sampleQuotes, FeedFilter.All, emptySet(), seed = 42)
        val second = buildFeed(sampleQuotes, FeedFilter.All, setOf("a"), seed = 42)
        assertEquals(first, second)
        assertEquals(sampleQuotes.toSet(), first.toSet())
    }

    @Test
    fun `own filter keeps only own quotes`() {
        val result = filterQuotes(sampleQuotes + sampleOwnQuote, FeedFilter.Own, emptySet())
        assertEquals(listOf(sampleOwnQuote), result)
    }

    @Test
    fun `adding a quote does not reorder the others`() {
        val before = buildFeed(sampleQuotes, FeedFilter.All, emptySet(), seed = 99)
        val after = buildFeed(sampleQuotes + sampleOwnQuote, FeedFilter.All, emptySet(), seed = 99)
        assertEquals(before, after - sampleOwnQuote)
    }

    @Test
    fun `different seeds give different orders`() {
        val orders = (1L..10L).map { seed -> buildFeed(sampleQuotes, FeedFilter.All, emptySet(), seed) }.toSet()
        assertTrue("Mischung wirkt nicht zufällig", orders.size > 1)
    }

    @Test
    fun `favorites feed is not shuffled`() {
        val feed = buildFeed(sampleQuotes, FeedFilter.Favorites, setOf("d", "a", "c"), seed = 7)
        assertEquals(listOf("a", "c", "d"), feed.map { it.id })
    }

    @Test
    fun `pickQuote falls back to all quotes when filter is empty`() {
        val quote = pickQuote(sampleQuotes, FeedFilter.Favorites, emptySet(), Random(1))
        assertTrue(quote in sampleQuotes)
    }

    @Test
    fun `pickQuote respects the filter`() {
        repeat(20) { seed ->
            val quote = pickQuote(sampleQuotes, FeedFilter.ByCategory(Category.GLUECK), emptySet(), Random(seed))
            assertTrue(quote!!.id in setOf("b", "e"))
        }
    }

    @Test
    fun `pickQuote returns null without quotes`() {
        assertNull(pickQuote(emptyList(), FeedFilter.All, emptySet()))
    }
}
