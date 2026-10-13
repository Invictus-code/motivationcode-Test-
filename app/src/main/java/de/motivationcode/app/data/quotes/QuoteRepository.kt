package de.motivationcode.app.data.quotes

import android.content.Context
import de.motivationcode.app.data.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface QuoteRepository {
    suspend fun allQuotes(): List<Quote>
}

/** Liest die mitgelieferten Zitate einmal aus assets/quotes.json und hält sie im Speicher. */
class AssetQuoteRepository(private val context: Context) : QuoteRepository {
    private val mutex = Mutex()
    private var cache: List<Quote>? = null

    override suspend fun allQuotes(): List<Quote> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            val text = context.assets.open(QUOTES_FILE).bufferedReader().use { it.readText() }
            parseQuotes(text)
        }.also { cache = it }
    }

    companion object {
        const val QUOTES_FILE = "quotes.json"
        private val json = Json { ignoreUnknownKeys = true }

        fun parseQuotes(text: String): List<Quote> = json.decodeFromString(text)
    }
}
