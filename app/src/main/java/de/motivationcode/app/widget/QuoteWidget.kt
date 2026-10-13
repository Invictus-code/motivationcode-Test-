package de.motivationcode.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.motivationcode.app.MainActivity
import de.motivationcode.app.R
import de.motivationcode.app.appContainer
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.quotes.pickQuote
import de.motivationcode.app.data.themes.QuoteFont
import de.motivationcode.app.data.themes.QuoteTheme
import de.motivationcode.app.data.themes.QuoteThemes
import kotlinx.coroutines.flow.first

/**
 * Homescreen-Widget mit einem Zitat. Welches Zitat gezeigt wird, steht im Glance-State
 * (pro Widget), damit es nur beim Tippen auf „Neu“ oder durch [WidgetRotationWorker] wechselt.
 */
class QuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = context.appContainer
        val initialQuotes = container.quoteRepository.quotes.first()
        val initialPrefs = container.preferencesRepository.preferences.first()

        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        if (initialQuotes.none { it.id == state[QUOTE_ID_KEY] }) {
            showNewQuote(context, id, update = false)
        }

        provideContent {
            // Live mitlesen: eigene Zitate und Theme können sich ändern, während das Widget läuft.
            val quotes by container.quoteRepository.quotes.collectAsState(initialQuotes)
            val prefs by container.preferencesRepository.preferences.collectAsState(initialPrefs)
            val quoteId = currentState(QUOTE_ID_KEY)
            val quote = quotes.firstOrNull { it.id == quoteId } ?: quotes.firstOrNull()
            WidgetContent(quote, QuoteThemes.byId(prefs.themeId))
        }
    }

    companion object {
        internal val QUOTE_ID_KEY = stringPreferencesKey("widget_quote_id")

        /** Wählt ein neues Zitat (passend zum Feed-Filter) für ein Widget. */
        suspend fun showNewQuote(context: Context, id: GlanceId, update: Boolean = true) {
            val container = context.appContainer
            val prefs = container.preferencesRepository.preferences.first()
            val quote = pickQuote(container.quoteRepository.quotes.first(), prefs.feedFilter, prefs.favoriteIds)
                ?: return
            updateAppWidgetState(context, id) { it[QUOTE_ID_KEY] = quote.id }
            if (update) QuoteWidget().update(context, id)
        }

        suspend fun showNewQuoteEverywhere(context: Context) {
            GlanceAppWidgetManager(context).getGlanceIds(QuoteWidget::class.java).forEach {
                showNewQuote(context, it)
            }
        }
    }
}

class RefreshQuoteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        QuoteWidget.showNewQuote(context, glanceId)
    }
}

@Composable
private fun WidgetContent(quote: Quote?, theme: QuoteTheme) {
    val textColor = ColorProvider(Color(theme.textColor))
    val fontFamily = when (theme.font) {
        QuoteFont.SERIF -> FontFamily.Serif
        QuoteFont.SANS -> FontFamily.SansSerif
        QuoteFont.CURSIVE -> FontFamily.Cursive
        QuoteFont.MONO -> FontFamily.Monospace
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .background(Color(theme.backgroundColors.last()))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = quote?.let { "„${it.text}“" } ?: "Zitate werden geladen …",
            style = TextStyle(color = textColor, fontSize = 16.sp, fontFamily = fontFamily),
            maxLines = 7,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.height(8.dp))
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = quote?.author?.takeIf { it.isNotBlank() }?.let { "— $it" } ?: "",
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                ),
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            Image(
                provider = ImageProvider(R.drawable.ic_refresh),
                contentDescription = "Neues Zitat",
                colorFilter = ColorFilter.tint(textColor),
                modifier = GlanceModifier
                    .size(40.dp)
                    .padding(8.dp)
                    .clickable(actionRunCallback<RefreshQuoteAction>()),
            )
        }
    }
}
