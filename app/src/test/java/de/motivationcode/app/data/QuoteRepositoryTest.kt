package de.motivationcode.app.data

import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.quotes.DefaultQuoteRepository
import de.motivationcode.app.testing.sampleQuotes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRepositoryTest {

    @Test
    fun `combines bundled and own quotes`() = runTest {
        val own = MutableStateFlow(listOf(OwnQuote("own-1", "Mein Spruch", author = null, createdAt = 1)))
        val repo = DefaultQuoteRepository(loadBundled = { sampleQuotes }, ownQuotes = own)

        val all = repo.quotes.first()
        assertEquals(sampleQuotes.size + 1, all.size)
        val mine = all.single { it.isOwn }
        assertEquals("Mein Spruch", mine.text)
        assertEquals("", mine.author)
        assertTrue(all.filterNot { it.isOwn } == sampleQuotes)
    }

    @Test
    fun `updates when own quotes change`() = runTest {
        val own = MutableStateFlow(emptyList<OwnQuote>())
        val repo = DefaultQuoteRepository(loadBundled = { sampleQuotes }, ownQuotes = own)
        assertEquals(sampleQuotes.size, repo.quotes.first().size)

        own.value = listOf(OwnQuote("own-1", "Neu", author = "Ich", createdAt = 1))
        assertEquals(sampleQuotes.size + 1, repo.quotes.first().size)
    }
}
