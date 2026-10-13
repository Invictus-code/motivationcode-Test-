package de.motivationcode.app.ui.themes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.settings.UserPreferencesRepository
import de.motivationcode.app.data.themes.QuoteTheme
import de.motivationcode.app.data.themes.QuoteThemes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemesUiState(
    val themes: List<QuoteTheme> = QuoteThemes.all,
    /** null, solange die Einstellungen noch laden. */
    val selectedId: String? = null,
)

class ThemesViewModel(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<ThemesUiState> = preferencesRepository.preferences
        .map { ThemesUiState(selectedId = it.themeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemesUiState())

    fun select(themeId: String) {
        viewModelScope.launch { preferencesRepository.setTheme(themeId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ThemesViewModel(this[APPLICATION_KEY]!!.appContainer.preferencesRepository)
            }
        }
    }
}
