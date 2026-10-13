package de.motivationcode.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.quotes.QuoteRepository
import de.motivationcode.app.data.quotes.filterQuotes
import de.motivationcode.app.data.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryItem(
    val filter: FeedFilter,
    val count: Int,
    val selected: Boolean,
)

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data object Error : CategoriesUiState
    data class Content(val items: List<CategoryItem>) : CategoriesUiState
}

class CategoriesViewModel(
    quoteRepository: QuoteRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val quotes = quoteRepository.quotes
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }

    val uiState: StateFlow<CategoriesUiState> =
        combine(quotes, preferencesRepository.preferences) { result, prefs ->
            result.fold(
                onSuccess = { all ->
                    val filters = listOf(FeedFilter.All, FeedFilter.Favorites, FeedFilter.Own) +
                        Category.entries.map { FeedFilter.ByCategory(it) }
                    CategoriesUiState.Content(
                        filters.map { filter ->
                            CategoryItem(
                                filter = filter,
                                count = filterQuotes(all, filter, prefs.favoriteIds).size,
                                selected = filter == prefs.feedFilter,
                            )
                        },
                    )
                },
                onFailure = { CategoriesUiState.Error },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState.Loading)

    /** Speichert die Auswahl und ruft danach [onSaved] auf (z. B. zum Zurücknavigieren). */
    fun select(filter: FeedFilter, onSaved: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.setFeedFilter(filter)
            onSaved()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = this[APPLICATION_KEY]!!.appContainer
                CategoriesViewModel(container.quoteRepository, container.preferencesRepository)
            }
        }
    }
}
