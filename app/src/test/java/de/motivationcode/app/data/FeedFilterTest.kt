package de.motivationcode.app.data

import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedFilterTest {

    @Test
    fun `serialize and deserialize round trip`() {
        val filters = listOf(FeedFilter.All, FeedFilter.Favorites) + Category.entries.map { FeedFilter.ByCategory(it) }
        filters.forEach { assertEquals(it, FeedFilter.deserialize(it.serialize())) }
    }

    @Test
    fun `unknown or missing values fall back to all`() {
        assertEquals(FeedFilter.All, FeedFilter.deserialize(null))
        assertEquals(FeedFilter.All, FeedFilter.deserialize("category:gibtsnicht"))
        assertEquals(FeedFilter.All, FeedFilter.deserialize("kaputt"))
    }
}
