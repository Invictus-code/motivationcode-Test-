package de.motivationcode.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.quotes.pickQuote
import kotlinx.coroutines.flow.first

class QuoteReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = applicationContext.appContainer
        val prefs = container.preferencesRepository.preferences.first()
        if (!prefs.reminders.enabled) return Result.success()

        val quote = pickQuote(container.quoteRepository.quotes.first(), prefs.feedFilter, prefs.favoriteIds)
        if (quote != null) ReminderNotifications.show(applicationContext, quote)

        // An die laufende Arbeit anhängen, damit sie sich nicht selbst abbricht.
        container.reminderScheduler.sync(prefs.reminders, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }
}
