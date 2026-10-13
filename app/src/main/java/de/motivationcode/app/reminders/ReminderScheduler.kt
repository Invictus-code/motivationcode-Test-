package de.motivationcode.app.reminders

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.motivationcode.app.data.settings.ReminderSettings
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Plant immer nur die nächste Erinnerung. Der Worker plant nach dem Anzeigen die folgende ein.
 * WorkManager übersteht Neustarts des Geräts, deshalb braucht es keinen eigenen Boot-Receiver.
 */
class ReminderScheduler(private val workManager: WorkManager) {

    fun sync(settings: ReminderSettings, policy: ExistingWorkPolicy) {
        if (!settings.enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val next = ReminderTimes.nextReminder(now, settings)
        val delay = Duration.between(now.atZone(zone), next.atZone(zone))
        val request = OneTimeWorkRequestBuilder<QuoteReminderWorker>()
            .setInitialDelay(delay)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, policy, request)
    }

    companion object {
        const val WORK_NAME = "quote_reminder"
    }
}
