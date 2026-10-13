package de.motivationcode.app.data

import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.quotes.AssetQuoteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Prüft die mitgelieferte quotes.json, damit Tippfehler beim Ergänzen sofort auffallen. */
class QuotesDataTest {

    private val quotes = AssetQuoteRepository.parseQuotes(
        File("src/main/assets/${AssetQuoteRepository.QUOTES_FILE}").readText(),
    )

    @Test
    fun `file contains quotes`() {
        assertTrue("zu wenige Zitate: ${quotes.size}", quotes.size >= 50)
    }

    @Test
    fun `ids are unique`() {
        val duplicates = quotes.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet<String>(), duplicates)
    }

    @Test
    fun `texts are unique`() {
        val duplicates = quotes.groupBy { it.text }.filterValues { it.size > 1 }.keys
        assertEquals(emptySet<String>(), duplicates)
    }

    @Test
    fun `every quote has text, author and a known category`() {
        val knownIds = Category.entries.map { it.id }.toSet()
        quotes.forEach { quote ->
            assertTrue("leerer Text: ${quote.id}", quote.text.isNotBlank())
            assertTrue("leerer Autor: ${quote.id}", quote.author.isNotBlank())
            assertTrue("keine Kategorie: ${quote.id}", quote.categories.isNotEmpty())
            val unknown = quote.categories - knownIds
            assertTrue("unbekannte Kategorie $unknown in ${quote.id}", unknown.isEmpty())
        }
    }

    @Test
    fun `texts carry no own quotation marks`() {
        // Die App setzt „…“ selbst; sonst stünden sie doppelt da.
        quotes.forEach { quote ->
            val first = quote.text.first()
            val last = quote.text.last()
            assertTrue("Anführungszeichen in ${quote.id}", first !in "„\"“»" && last !in "“\"«")
        }
    }

    @Test
    fun `translated quotes name their source`() {
        quotes.filter { it.translated }.forEach { quote ->
            assertTrue("Quelle fehlt bei Übersetzung: ${quote.id}", !quote.source.isNullOrBlank())
        }
    }

    @Test
    fun `every category has enough quotes`() {
        Category.entries.forEach { category ->
            val count = quotes.count { category.id in it.categories }
            assertTrue("${category.title} hat nur $count Zitate", count >= 5)
        }
    }
}
