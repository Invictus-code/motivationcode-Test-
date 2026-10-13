package de.motivationcode.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.quotes.QuoteRepository
import de.motivationcode.app.data.quotes.buildFeed
import de.motivationcode.app.data.settings.UserPreferencesRepository
import de.motivationcode.app.data.themes.QuoteTheme
import de.motivationcode.app.data.themes.QuoteThemes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data object Error : FeedUiState
    data class Content(
        val quotes: List<Quote>,
        val favoriteIds: Set<String>,
        val filter: FeedFilter,
        val theme: QuoteTheme,
    ) : FeedUiState
}

class FeedViewModel(
    quoteRepository: QuoteRepository,
    private val preferencesRepository: UserPreferencesRepository,
    shuffleSeed: Long = Random.nextLong(),
) : ViewModel() {

    private val quotes = quoteRepository.quotes
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    val uiState: StateFlow<FeedUiState> =
        combine(quotes, preferencesRepository.preferences) { result, prefs ->
            result.fold(
                onSuccess = { all ->
                    FeedUiState.Content(
                        quotes = buildFeed(all, prefs.feedFilter, prefs.favoriteIds, shuffleSeed),
                        favoriteIds = prefs.favoriteIds,
                        filter = prefs.feedFilter,
                        theme = QuoteThemes.byId(prefs.themeId),
                    )
                },
                onFailure = { FeedUiState.Error },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState.Loading)

    fun toggleFavorite(quoteId: String) {
        viewModelScope.launch { preferencesRepository.toggleFavorite(quoteId) }
    }

    fun showAllQuotes() {
        viewModelScope.launch { preferencesRepository.setFeedFilter(FeedFilter.All) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                FeedViewModel(container.quoteRepository, container.preferencesRepository)
            }
        }
    }
}
