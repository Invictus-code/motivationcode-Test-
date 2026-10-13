package de.motivationcode.app.ui

import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.settings.UserPreferences
import de.motivationcode.app.testing.FakeQuoteRepository
import de.motivationcode.app.testing.FakeUserPreferencesRepository
import de.motivationcode.app.testing.MainDispatcherRule
import de.motivationcode.app.ui.feed.FeedUiState
import de.motivationcode.app.ui.feed.FeedViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun FeedViewModel.content(): FeedUiState.Content =
        uiState.first { it is FeedUiState.Content } as FeedUiState.Content

    @Test
    fun `initial state is loading`() {
        val viewModel = FeedViewModel(FakeQuoteRepository(), FakeUserPreferencesRepository())
        assertEquals(FeedUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `shows all quotes by default`() = runTest {
        val viewModel = FeedViewModel(FakeQuoteRepository(), FakeUserPreferencesRepository(), shuffleSeed = 1)
        val state = viewModel.content()
        assertEquals(5, state.quotes.size)
        assertEquals(FeedFilter.All, state.filter)
    }

    @Test
    fun `category filter from preferences is applied`() = runTest {
        val prefs = FakeUserPreferencesRepository(UserPreferences(feedFilter = FeedFilter.ByCategory(Category.GLUECK)))
        val viewModel = FeedViewModel(FakeQuoteRepository(), prefs, shuffleSeed = 1)
        assertEquals(setOf("b", "e"), viewModel.content().quotes.map { it.id }.toSet())
    }

    @Test
    fun `toggleFavorite updates favorites without reshuffling`() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = FeedViewModel(FakeQuoteRepository(), prefs, shuffleSeed = 1)
        val before = viewModel.content()

        viewModel.toggleFavorite("c")

        val after = viewModel.uiState.first { (it as? FeedUiState.Content)?.favoriteIds == setOf("c") }
            as FeedUiState.Content
        assertEquals(before.quotes, after.quotes)
        assertEquals(setOf("c"), prefs.state.value.favoriteIds)
    }

    @Test
    fun `empty favorites give an empty content state`() = runTest {
        val prefs = FakeUserPreferencesRepository(UserPreferences(feedFilter = FeedFilter.Favorites))
        val viewModel = FeedViewModel(FakeQuoteRepository(), prefs)
        assertTrue(viewModel.content().quotes.isEmpty())
    }

    @Test
    fun `showAllQuotes resets the filter`() = runTest {
        val prefs = FakeUserPreferencesRepository(UserPreferences(feedFilter = FeedFilter.Favorites))
        val viewModel = FeedViewModel(FakeQuoteRepository(), prefs)
        viewModel.content()

        viewModel.showAllQuotes()

        assertEquals(FeedFilter.All, prefs.state.value.feedFilter)
    }

    @Test
    fun `loading error gives error state`() = runTest {
        val viewModel = FeedViewModel(FakeQuoteRepository(error = IOException("kaputt")), FakeUserPreferencesRepository())
        assertEquals(FeedUiState.Error, viewModel.uiState.first { it != FeedUiState.Loading })
    }
}
