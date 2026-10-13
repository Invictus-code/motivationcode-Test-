package de.motivationcode.app.data

import androidx.datastore.core.DataStoreFactory
import de.motivationcode.app.data.own.DataStoreOwnQuoteRepository
import de.motivationcode.app.data.own.OwnQuotesSerializer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Testet gegen einen echten DataStore in einer temporären Datei. */
class OwnQuoteRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository(): DataStoreOwnQuoteRepository {
        var clock = 0L
        var counter = 0
        val store = DataStoreFactory.create(
            serializer = OwnQuotesSerializer,
            scope = backgroundScope,
            produceFile = { File(tmp.root, "own_quotes.json") },
        )
        return DataStoreOwnQuoteRepository(store, now = { ++clock }, newId = { "own-${++counter}" })
    }

    @Test
    fun `starts empty`() = runTest {
        assertTrue(repository().ownQuotes.first().isEmpty())
    }

    @Test
    fun `add stores cleaned text, newest first`() = runTest {
        val repo = repository()
        repo.add("„Erster“", "")
        repo.add("  Zweiter  ", " Ich ")

        val quotes = repo.ownQuotes.first()
        assertEquals(listOf("Zweiter", "Erster"), quotes.map { it.text })
        assertEquals(listOf("Ich", null), quotes.map { it.author })
    }

    @Test
    fun `blank text is not saved`() = runTest {
        val repo = repository()
        assertNull(repo.add("   ", "Ich"))
        assertTrue(repo.ownQuotes.first().isEmpty())
    }

    @Test
    fun `update changes text and author`() = runTest {
        val repo = repository()
        val quote = repo.add("Alt", "")!!
        repo.update(quote.id, "Neu", "Ich")

        val updated = repo.ownQuotes.first().single()
        assertEquals("Neu", updated.text)
        assertEquals("Ich", updated.author)
        assertEquals(quote.createdAt, updated.createdAt)
    }

    @Test
    fun `update with blank text keeps the old text`() = runTest {
        val repo = repository()
        val quote = repo.add("Alt", "")!!
        repo.update(quote.id, "  ", "")
        assertEquals("Alt", repo.ownQuotes.first().single().text)
    }

    @Test
    fun `delete and restore bring back the same quote at its place`() = runTest {
        val repo = repository()
        repo.add("Eins", "")
        val two = repo.add("Zwei", "")!!
        repo.add("Drei", "")

        repo.delete(two.id)
        assertEquals(listOf("Drei", "Eins"), repo.ownQuotes.first().map { it.text })

        repo.restore(two)
        repo.restore(two) // doppeltes Rückgängig erzeugt kein Duplikat
        assertEquals(listOf("Drei", "Zwei", "Eins"), repo.ownQuotes.first().map { it.text })
    }
}
