package de.motivationcode.app.ui

import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.testing.FakeOwnQuoteRepository
import de.motivationcode.app.testing.MainDispatcherRule
import de.motivationcode.app.ui.own.OwnQuotesUiState
import de.motivationcode.app.ui.own.OwnQuotesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnQuotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val quote = OwnQuote("own-1", "Mein Spruch", author = null, createdAt = 1)

    @Test
    fun `initial state is loading, then content`() = runTest {
        val viewModel = OwnQuotesViewModel(FakeOwnQuoteRepository(listOf(quote)))
        assertEquals(OwnQuotesUiState.Loading, viewModel.uiState.value)
        assertEquals(OwnQuotesUiState.Content(listOf(quote)), viewModel.uiState.first { it is OwnQuotesUiState.Content })
    }

    @Test
    fun `save without id adds, with id updates`() {
        val repo = FakeOwnQuoteRepository()
        val viewModel = OwnQuotesViewModel(repo)

        viewModel.save(null, "Neu", "Ich")
        viewModel.save("own-1", "Geändert", "")

        assertEquals(listOf("add:Neu|Ich", "update:own-1:Geändert|"), repo.calls)
    }

    @Test
    fun `delete and restore are passed on`() {
        val repo = FakeOwnQuoteRepository()
        val viewModel = OwnQuotesViewModel(repo)

        viewModel.delete(quote)
        viewModel.restore(quote)

        assertEquals(listOf("delete:own-1", "restore:own-1"), repo.calls)
    }
}
