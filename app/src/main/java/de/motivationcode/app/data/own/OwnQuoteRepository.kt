package de.motivationcode.app.data.own

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface OwnQuoteRepository {
    /** Neueste zuerst. */
    val ownQuotes: Flow<List<OwnQuote>>

    /** Legt ein Zitat an; gibt null zurück, wenn der Text leer ist. */
    suspend fun add(text: String, author: String): OwnQuote?
    suspend fun update(id: String, text: String, author: String)
    suspend fun delete(id: String)

    /** Für „Rückgängig“: stellt ein gelöschtes Zitat mit gleicher ID wieder her. */
    suspend fun restore(quote: OwnQuote)
}

class DataStoreOwnQuoteRepository(
    private val dataStore: DataStore<List<OwnQuote>>,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { OwnQuote.ID_PREFIX + UUID.randomUUID() },
) : OwnQuoteRepository {

    override val ownQuotes: Flow<List<OwnQuote>> =
        dataStore.data.map { list -> list.sortedByDescending { it.createdAt } }

    override suspend fun add(text: String, author: String): OwnQuote? {
        val cleanText = normalizeQuoteText(text)
        if (cleanText.isEmpty()) return null
        val quote = OwnQuote(id = newId(), text = cleanText, author = normalizeAuthor(author), createdAt = now())
        dataStore.updateData { it + quote }
        return quote
    }

    override suspend fun update(id: String, text: String, author: String) {
        val cleanText = normalizeQuoteText(text)
        if (cleanText.isEmpty()) return
        dataStore.updateData { list ->
            list.map { if (it.id == id) it.copy(text = cleanText, author = normalizeAuthor(author)) else it }
        }
    }

    override suspend fun delete(id: String) {
        dataStore.updateData { list -> list.filterNot { it.id == id } }
    }

    override suspend fun restore(quote: OwnQuote) {
        dataStore.updateData { list -> if (list.any { it.id == quote.id }) list else list + quote }
    }
}
