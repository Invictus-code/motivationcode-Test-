package de.motivationcode.app.ui.own

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.own.OwnQuoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OwnQuotesUiState {
    data object Loading : OwnQuotesUiState
    data object Error : OwnQuotesUiState
    data class Content(val quotes: List<OwnQuote>) : OwnQuotesUiState
}

class OwnQuotesViewModel(
    private val repository: OwnQuoteRepository,
) : ViewModel() {

    val uiState: StateFlow<OwnQuotesUiState> = repository.ownQuotes
        .map<_, OwnQuotesUiState> { OwnQuotesUiState.Content(it) }
        .catch { emit(OwnQuotesUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OwnQuotesUiState.Loading)

    /** Neues Zitat, wenn [id] null ist, sonst Änderung eines bestehenden. */
    fun save(id: String?, text: String, author: String) {
        viewModelScope.launch {
            if (id == null) repository.add(text, author) else repository.update(id, text, author)
        }
    }

    fun delete(quote: OwnQuote) {
        viewModelScope.launch { repository.delete(quote.id) }
    }

    fun restore(quote: OwnQuote) {
        viewModelScope.launch { repository.restore(quote) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                OwnQuotesViewModel(this[APPLICATION_KEY]!!.appContainer.ownQuoteRepository)
            }
        }
    }
}
