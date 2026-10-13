package de.motivationcode.app.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Wechselt das Zitat in allen Widgets alle paar Stunden. */
class WidgetRotationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        QuoteWidget.showNewQuoteEverywhere(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_rotation"
        private const val INTERVAL_HOURS = 3L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRotationWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
                .setInitialDelay(INTERVAL_HOURS, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
