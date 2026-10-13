package de.motivationcode.app.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRotationWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRotationWorker.cancel(context)
    }
}
