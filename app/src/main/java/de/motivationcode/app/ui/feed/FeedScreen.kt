package de.motivationcode.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.motivationcode.app.R
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.themes.QuoteTheme
import de.motivationcode.app.data.themes.QuoteThemes
import de.motivationcode.app.share.QuoteSharer
import de.motivationcode.app.ui.components.SystemBarsAppearance
import de.motivationcode.app.ui.theme.TagesmotivationTheme
import de.motivationcode.app.ui.theme.backgroundBrush
import de.motivationcode.app.ui.theme.toFontFamily
import kotlinx.coroutines.launch

@Composable
fun FeedRoute(
    onOpenCategories: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: FeedViewModel = viewModel(factory = FeedViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FeedScreen(
        uiState = uiState,
        onToggleFavorite = viewModel::toggleFavorite,
        onShare = { quote, theme -> scope.launch { QuoteSharer.share(context, quote, theme) } },
        onShowAll = viewModel::showAllQuotes,
        onOpenCategories = onOpenCategories,
        onOpenThemes = onOpenThemes,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun FeedScreen(
    uiState: FeedUiState,
    onToggleFavorite: (String) -> Unit,
    onShare: (Quote, QuoteTheme) -> Unit,
    onShowAll: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = (uiState as? FeedUiState.Content)?.theme ?: QuoteThemes.default
    SystemBarsAppearance(lightBackground = theme.isLight)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundBrush()),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color(theme.textColor)) {
            when (uiState) {
                FeedUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = LocalContentColor.current,
                )

                FeedUiState.Error -> CenteredMessage(
                    title = "Zitate konnten nicht geladen werden.",
                    text = "Bitte starte die App neu.",
                )

                is FeedUiState.Content -> if (uiState.quotes.isEmpty()) {
                    EmptyFeed(filter = uiState.filter, onShowAll = onShowAll)
                } else {
                    QuotePager(
                        state = uiState,
                        onToggleFavorite = onToggleFavorite,
                        onShare = { onShare(it, uiState.theme) },
                        onOpenCategories = onOpenCategories,
                    )
                }
            }

            FilterChip(
                title = (uiState as? FeedUiState.Content)?.filter?.title ?: FeedFilter.All.title,
                onClick = onOpenCategories,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(top = 8.dp),
            )
            BottomActions(
                onOpenCategories = onOpenCategories,
                onOpenThemes = onOpenThemes,
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun QuotePager(
    state: FeedUiState.Content,
    onToggleFavorite: (String) -> Unit,
    onShare: (Quote) -> Unit,
    onOpenCategories: () -> Unit,
) {
    // Neuer Filter → wieder oben anfangen. Die letzte Seite ist der „Ende“-Hinweis.
    val pagerState = key(state.filter) { rememberPagerState { state.quotes.size + 1 } }
    val scope = rememberCoroutineScope()

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        key = { page -> state.quotes.getOrNull(page)?.id ?: "end" },
    ) { page ->
        val quote = state.quotes.getOrNull(page)
        if (quote == null) {
            EndPage(
                filter = state.filter,
                onRestart = { scope.launch { pagerState.scrollToPage(0) } },
                onOpenCategories = onOpenCategories,
            )
        } else {
            QuotePage(
                quote = quote,
                theme = state.theme,
                isFavorite = quote.id in state.favoriteIds,
                showSwipeHint = page == 0,
                onToggleFavorite = { onToggleFavorite(quote.id) },
                onShare = { onShare(quote) },
            )
        }
    }
}

@Composable
private fun QuotePage(
    quote: Quote,
    theme: QuoteTheme,
    isFavorite: Boolean,
    showSwipeHint: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    val fontFamily = theme.font.toFontFamily()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 32.dp, vertical = 72.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "„${quote.text}“",
                style = quoteTextStyle(quote.text.length).copy(fontFamily = fontFamily),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            val sourceLine = listOfNotNull(quote.source, "Übersetzung".takeIf { quote.translated })
                .joinToString(" · ")
            if (sourceLine.isNotEmpty()) {
                Text(
                    text = sourceLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(56.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Aus Favoriten entfernen" else "Zu Favoriten hinzufügen",
                        modifier = Modifier.size(32.dp),
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(56.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Zitat als Bild teilen",
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }
        if (showSwipeHint) {
            Text(
                text = "Nach oben wischen für das nächste Zitat",
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = 0.85f),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

private fun quoteTextStyle(length: Int): TextStyle {
    val size = when {
        length <= 60 -> 32
        length <= 120 -> 28
        length <= 200 -> 24
        else -> 21
    }
    return TextStyle(fontSize = size.sp, lineHeight = (size * 1.3f).sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun FilterChip(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = LocalContentColor.current.copy(alpha = 0.14f),
        contentColor = LocalContentColor.current,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Kategorie wählen")
        }
    }
}

@Composable
private fun BottomActions(
    onOpenCategories: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomAction(ImageVector.vectorResource(R.drawable.ic_category), "Kategorien", onOpenCategories)
        BottomAction(ImageVector.vectorResource(R.drawable.ic_palette), "Themes", onOpenThemes)
        BottomAction(Icons.Filled.Settings, "Einstellungen", onOpenSettings)
    }
}

@Composable
private fun BottomAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = LocalContentColor.current.copy(alpha = 0.14f),
        contentColor = LocalContentColor.current,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EmptyFeed(filter: FeedFilter, onShowAll: () -> Unit) {
    val (title, text) = if (filter == FeedFilter.Favorites) {
        "Noch keine Favoriten" to "Tippe bei einem Zitat auf das Herz, dann landet es hier."
    } else {
        "Keine Zitate gefunden" to "In dieser Auswahl gibt es gerade nichts."
    }
    CenteredMessage(title = title, text = text) {
        OutlinedButton(onClick = onShowAll) { Text("Alle Zitate zeigen") }
    }
}

@Composable
private fun EndPage(filter: FeedFilter, onRestart: () -> Unit, onOpenCategories: () -> Unit) {
    CenteredMessage(
        title = "Das war's für „${filter.title}“",
        text = "Du hast alle Zitate dieser Auswahl gesehen.",
    ) {
        OutlinedButton(onClick = onRestart) { Text("Nochmal von vorn") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenCategories) { Text("Andere Kategorie wählen") }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    text: String,
    actions: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            actions()
        }
    }
}

private val previewQuotes = listOf(
    Quote(
        id = "preview",
        text = "Es gibt nichts Gutes, außer: Man tut es.",
        author = "Erich Kästner",
        source = "Epigramm „Moral“",
        categories = listOf("motivation"),
    ),
)

@Preview(name = "Inhalt", showSystemUi = true)
@Composable
private fun FeedContentPreview() {
    TagesmotivationTheme {
        FeedScreen(
            uiState = FeedUiState.Content(previewQuotes, setOf("preview"), FeedFilter.All, QuoteThemes.default),
            onToggleFavorite = {}, onShare = { _, _ -> }, onShowAll = {},
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {},
        )
    }
}

@Preview(name = "Leer (Favoriten)", showSystemUi = true)
@Composable
private fun FeedEmptyPreview() {
    TagesmotivationTheme {
        FeedScreen(
            uiState = FeedUiState.Content(emptyList(), emptySet(), FeedFilter.Favorites, QuoteThemes.byId("papier")),
            onToggleFavorite = {}, onShare = { _, _ -> }, onShowAll = {},
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {},
        )
    }
}

@Preview(name = "Lädt", showSystemUi = true)
@Composable
private fun FeedLoadingPreview() {
    TagesmotivationTheme {
        FeedScreen(
            uiState = FeedUiState.Loading,
            onToggleFavorite = {}, onShare = { _, _ -> }, onShowAll = {},
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {},
        )
    }
}

@Preview(name = "Fehler", showSystemUi = true)
@Composable
private fun FeedErrorPreview() {
    TagesmotivationTheme {
        FeedScreen(
            uiState = FeedUiState.Error,
            onToggleFavorite = {}, onShare = { _, _ -> }, onShowAll = {},
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {},
        )
    }
}
