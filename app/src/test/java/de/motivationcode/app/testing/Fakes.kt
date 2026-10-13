package de.motivationcode.app.testing

import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.quotes.QuoteRepository
import de.motivationcode.app.data.settings.UserPreferences
import de.motivationcode.app.data.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime

class FakeQuoteRepository(
    private val quotes: List<Quote> = sampleQuotes,
    private val error: Throwable? = null,
) : QuoteRepository {
    override suspend fun allQuotes(): List<Quote> {
        error?.let { throw it }
        return quotes
    }
}

class FakeUserPreferencesRepository(initial: UserPreferences = UserPreferences()) : UserPreferencesRepository {
    val state = MutableStateFlow(initial)
    override val preferences = state

    override suspend fun toggleFavorite(quoteId: String) = state.update {
        val favorites = if (quoteId in it.favoriteIds) it.favoriteIds - quoteId else it.favoriteIds + quoteId
        it.copy(favoriteIds = favorites)
    }

    override suspend fun setFeedFilter(filter: FeedFilter) = state.update { it.copy(feedFilter = filter) }
    override suspend fun setTheme(themeId: String) = state.update { it.copy(themeId = themeId) }
    override suspend fun setRemindersEnabled(enabled: Boolean) =
        state.update { it.copy(reminders = it.reminders.copy(enabled = enabled)) }
    override suspend fun setRemindersPerDay(perDay: Int) =
        state.update { it.copy(reminders = it.reminders.copy(perDay = perDay)) }
    override suspend fun setReminderStart(time: LocalTime) =
        state.update { it.copy(reminders = it.reminders.copy(start = time)) }
    override suspend fun setReminderEnd(time: LocalTime) =
        state.update { it.copy(reminders = it.reminders.copy(end = time)) }
}

val sampleQuotes = listOf(
    Quote("a", "Text A", "Autor A", categories = listOf("mut", "motivation")),
    Quote("b", "Text B", "Autor B", categories = listOf("glueck")),
    Quote("c", "Text C", "Autor C", categories = listOf("mut")),
    Quote("d", "Text D", "Autor D", categories = listOf("weisheit")),
    Quote("e", "Text E", "Autor E", categories = listOf("liebe", "glueck")),
)
