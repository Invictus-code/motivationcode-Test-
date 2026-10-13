package de.motivationcode.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.settings.ReminderSettings
import de.motivationcode.app.data.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Content(val reminders: ReminderSettings) : SettingsUiState
}

/**
 * Schreibt nur Einstellungen. Das eigentliche Planen der Erinnerungen passiert zentral in
 * MotivationApp, sobald sich die Einstellungen ändern.
 */
class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesRepository.preferences
        .map<_, SettingsUiState> { SettingsUiState.Content(it.reminders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setRemindersEnabled(enabled) }
    }

    fun setRemindersPerDay(perDay: Int) {
        viewModelScope.launch { preferencesRepository.setRemindersPerDay(perDay) }
    }

    fun setStart(time: LocalTime) {
        viewModelScope.launch { preferencesRepository.setReminderStart(time) }
    }

    fun setEnd(time: LocalTime) {
        viewModelScope.launch { preferencesRepository.setReminderEnd(time) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SettingsViewModel(this[APPLICATION_KEY]!!.appContainer.preferencesRepository)
            }
        }
    }
}
