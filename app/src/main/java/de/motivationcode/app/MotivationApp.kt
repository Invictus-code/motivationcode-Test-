package de.motivationcode.app

import android.app.Application
import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingWorkPolicy
import de.motivationcode.app.di.AppContainer
import de.motivationcode.app.reminders.ReminderNotifications
import de.motivationcode.app.widget.QuoteWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MotivationApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderNotifications.createChannel(this)

        // Erinnerungen folgen den Einstellungen: beim App-Start nur sicherstellen, dass etwas geplant ist
        // (KEEP), bei jeder Änderung neu planen (REPLACE).
        appScope.launch {
            var first = true
            container.preferencesRepository.preferences
                .map { it.reminders }
                .distinctUntilChanged()
                .collect { settings ->
                    val policy = if (first) ExistingWorkPolicy.KEEP else ExistingWorkPolicy.REPLACE
                    first = false
                    container.reminderScheduler.sync(settings, policy)
                }
        }

        // Widget sofort neu zeichnen, wenn sich das Theme ändert.
        appScope.launch {
            container.preferencesRepository.preferences
                .map { it.themeId }
                .distinctUntilChanged()
                .drop(1)
                .collect { QuoteWidget().updateAll(this@MotivationApp) }
        }
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MotivationApp).container
