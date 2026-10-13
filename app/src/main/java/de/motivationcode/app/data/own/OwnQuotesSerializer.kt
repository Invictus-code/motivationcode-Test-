package de.motivationcode.app.data.own

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Speichert die eigenen Zitate als JSON-Datei (über DataStore, also sicher gegen halbe Schreibvorgänge). */
object OwnQuotesSerializer : Serializer<List<OwnQuote>> {
    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: List<OwnQuote> = emptyList()

    override suspend fun readFrom(input: InputStream): List<OwnQuote> =
        try {
            json.decodeFromString(input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("own_quotes.json ist beschädigt", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("own_quotes.json ist beschädigt", e)
        }

    override suspend fun writeTo(t: List<OwnQuote>, output: OutputStream) {
        output.write(json.encodeToString(t).encodeToByteArray())
    }
}
