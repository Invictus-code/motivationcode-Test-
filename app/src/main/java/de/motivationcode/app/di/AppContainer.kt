package de.motivationcode.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import de.motivationcode.app.data.own.DataStoreOwnQuoteRepository
import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.own.OwnQuoteRepository
import de.motivationcode.app.data.own.OwnQuotesSerializer
import de.motivationcode.app.data.quotes.BundledQuotes
import de.motivationcode.app.data.quotes.DefaultQuoteRepository
import de.motivationcode.app.data.quotes.QuoteRepository
import de.motivationcode.app.data.settings.DataStoreUserPreferencesRepository
import de.motivationcode.app.data.settings.UserPreferencesRepository
import de.motivationcode.app.reminders.ReminderScheduler

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Liegt wie die Einstellungen unter files/datastore/ und wird damit auch gesichert (siehe backup_rules.xml).
private val Context.ownQuotesStore: DataStore<List<OwnQuote>> by dataStore(
    fileName = "own_quotes.json",
    serializer = OwnQuotesSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { emptyList() },
)

/** Einfache manuelle Dependency Injection: eine Instanz pro App-Prozess. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val ownQuoteRepository: OwnQuoteRepository = DataStoreOwnQuoteRepository(appContext.ownQuotesStore)
    val quoteRepository: QuoteRepository = DefaultQuoteRepository(
        loadBundled = BundledQuotes(appContext)::load,
        ownQuotes = ownQuoteRepository.ownQuotes,
    )
    val preferencesRepository: UserPreferencesRepository =
        DataStoreUserPreferencesRepository(appContext.dataStore)
    val reminderScheduler: ReminderScheduler by lazy {
        ReminderScheduler(WorkManager.getInstance(appContext))
    }
}
