package de.motivationcode.app.ui

import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.settings.UserPreferences
import de.motivationcode.app.testing.FakeQuoteRepository
import de.motivationcode.app.testing.FakeUserPreferencesRepository
import de.motivationcode.app.testing.MainDispatcherRule
import de.motivationcode.app.ui.categories.CategoriesUiState
import de.motivationcode.app.ui.categories.CategoriesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `items show counts and the current selection`() = runTest {
        val prefs = FakeUserPreferencesRepository(
            UserPreferences(favoriteIds = setOf("a"), feedFilter = FeedFilter.ByCategory(Category.MUT)),
        )
        val viewModel = CategoriesViewModel(FakeQuoteRepository(), prefs)

        val items = (viewModel.uiState.first { it is CategoriesUiState.Content } as CategoriesUiState.Content).items
        val byFilter = items.associateBy { it.filter }

        assertEquals(5, byFilter.getValue(FeedFilter.All).count)
        assertEquals(1, byFilter.getValue(FeedFilter.Favorites).count)
        assertEquals(2, byFilter.getValue(FeedFilter.ByCategory(Category.MUT)).count)
        assertEquals(listOf(FeedFilter.ByCategory(Category.MUT)), items.filter { it.selected }.map { it.filter })
    }

    @Test
    fun `select saves the filter before calling back`() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = CategoriesViewModel(FakeQuoteRepository(), prefs)
        var savedFilterAtCallback: FeedFilter? = null

        viewModel.select(FeedFilter.Favorites) { savedFilterAtCallback = prefs.state.value.feedFilter }

        assertEquals(FeedFilter.Favorites, savedFilterAtCallback)
        assertTrue(prefs.state.value.feedFilter == FeedFilter.Favorites)
    }
}
