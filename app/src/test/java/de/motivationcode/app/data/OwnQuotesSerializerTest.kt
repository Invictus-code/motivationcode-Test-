package de.motivationcode.app.data

import androidx.datastore.core.CorruptionException
import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.own.OwnQuotesSerializer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class OwnQuotesSerializerTest {

    @Test
    fun `round trip keeps all fields`() = runTest {
        val quotes = listOf(
            OwnQuote("own-1", "Text mit „Zeichen“ und Umlauten äöü", author = "Ich", createdAt = 42),
            OwnQuote("own-2", "Ohne Autor", author = null, createdAt = 7),
        )
        val out = ByteArrayOutputStream()
        OwnQuotesSerializer.writeTo(quotes, out)

        val read = OwnQuotesSerializer.readFrom(ByteArrayInputStream(out.toByteArray()))
        assertEquals(quotes, read)
    }

    @Test
    fun `unknown fields from a newer app version are ignored`() = runTest {
        val json = """[{"id":"own-1","text":"Hallo","createdAt":1,"neuesFeld":true}]"""
        val read = OwnQuotesSerializer.readFrom(ByteArrayInputStream(json.toByteArray()))
        assertEquals(listOf(OwnQuote("own-1", "Hallo", author = null, createdAt = 1)), read)
    }

    @Test(expected = CorruptionException::class)
    fun `broken file is reported as corruption`() = runTest {
        OwnQuotesSerializer.readFrom(ByteArrayInputStream("{kaputt".toByteArray()))
    }
}
