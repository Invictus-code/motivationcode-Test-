package de.motivationcode.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import de.motivationcode.app.data.quotes.AssetQuoteRepository
import de.motivationcode.app.data.quotes.QuoteRepository
import de.motivationcode.app.data.settings.DataStoreUserPreferencesRepository
import de.motivationcode.app.data.settings.UserPreferencesRepository
import de.motivationcode.app.reminders.ReminderScheduler

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/** Einfache manuelle Dependency Injection: eine Instanz pro App-Prozess. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val quoteRepository: QuoteRepository = AssetQuoteRepository(appContext)
    val preferencesRepository: UserPreferencesRepository =
        DataStoreUserPreferencesRepository(appContext.dataStore)
    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(WorkManager.getInstance(appContext))
    }
}
