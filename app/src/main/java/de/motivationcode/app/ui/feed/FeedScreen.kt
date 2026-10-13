package de.motivationcode.app.ui.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
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
    onOpenOwnQuotes: () -> Unit,
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
        onOpenOwnQuotes = onOpenOwnQuotes,
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
    onOpenOwnQuotes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = (uiState as? FeedUiState.Content)?.theme ?: QuoteThemes.default
    SystemBarsAppearance(lightBackground = theme.isLight)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundBrush()),
    ) {
        // Wenig Höhe (Querformat): Aktionen wandern nach oben, Text wird kleiner.
        val compact = maxHeight < 480.dp
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
                    EmptyFeed(filter = uiState.filter, onShowAll = onShowAll, onOpenOwnQuotes = onOpenOwnQuotes)
                } else {
                    QuotePager(
                        state = uiState,
                        compact = compact,
                        onToggleFavorite = onToggleFavorite,
                        onShare = { onShare(it, uiState.theme) },
                        onOpenCategories = onOpenCategories,
                    )
                }
            }

            val filterTitle = (uiState as? FeedUiState.Content)?.filter?.title ?: FeedFilter.All.title
            val actions = listOf(
                FeedAction(ImageVector.vectorResource(R.drawable.ic_category), "Kategorien", onOpenCategories),
                FeedAction(ImageVector.vectorResource(R.drawable.ic_palette), "Themes", onOpenThemes),
                FeedAction(Icons.Filled.Settings, "Einstellungen", onOpenSettings),
            )
            val topInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            if (compact) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(topInsets)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(title = filterTitle, onClick = onOpenCategories)
                    Spacer(Modifier.weight(1f))
                    actions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(action.icon, contentDescription = action.label)
                        }
                    }
                }
            } else {
                FilterChip(
                    title = filterTitle,
                    onClick = onOpenCategories,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(topInsets)
                        .padding(top = 8.dp),
                )
                BottomActions(
                    actions = actions,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        .padding(bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun QuotePager(
    state: FeedUiState.Content,
    compact: Boolean,
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
                compact = compact,
                showSwipeHint = page == 0 && !compact,
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
    compact: Boolean,
    showSwipeHint: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    val fontFamily = theme.font.toFontFamily()
    // Oben Platz für den Kategorie-Chip, unten für die Aktionsleiste (im Querformat ist die oben).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = if (compact) 64.dp else 72.dp,
                bottom = if (compact) 8.dp else 96.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier.widthIn(max = if (compact) 720.dp else 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "„${quote.text}“",
                style = quoteTextStyle(quote.text.length, compact).copy(fontFamily = fontFamily),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (compact) 12.dp else 24.dp))
            if (quote.author.isNotBlank()) {
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = fontFamily),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            val sourceLine = listOfNotNull(
                quote.source,
                "Übersetzung".takeIf { quote.translated },
                "Eigenes Zitat".takeIf { quote.isOwn },
            ).joinToString(" · ")
            if (sourceLine.isNotEmpty()) {
                Text(
                    text = sourceLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.height(if (compact) 8.dp else 32.dp))
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
        Spacer(Modifier.weight(1f))
        if (showSwipeHint) {
            Text(
                text = "Nach oben wischen für das nächste Zitat",
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun quoteTextStyle(length: Int, compact: Boolean): TextStyle {
    val base = when {
        length <= 60 -> 32f
        length <= 120 -> 28f
        length <= 200 -> 24f
        else -> 21f
    }
    val size = if (compact) base * 0.8f else base
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

private class FeedAction(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun BottomActions(actions: List<FeedAction>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        actions.forEach { BottomAction(it) }
    }
}

@Composable
private fun BottomAction(action: FeedAction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = action.onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .background(LocalContentColor.current.copy(alpha = 0.14f), CircleShape),
        ) {
            Icon(action.icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun EmptyFeed(filter: FeedFilter, onShowAll: () -> Unit, onOpenOwnQuotes: () -> Unit) {
    val (title, text) = when (filter) {
        FeedFilter.Favorites -> "Noch keine Favoriten" to "Tippe bei einem Zitat auf das Herz, dann landet es hier."
        FeedFilter.Own -> "Noch keine eigenen Zitate" to "Schreib deinen ersten eigenen Spruch auf."
        else -> "Keine Zitate gefunden" to "In dieser Auswahl gibt es gerade nichts."
    }
    CenteredMessage(title = title, text = text) {
        if (filter == FeedFilter.Own) {
            ThemedOutlinedButton("Zitat hinzufügen", onOpenOwnQuotes)
            Spacer(Modifier.height(8.dp))
        }
        ThemedOutlinedButton("Alle Zitate zeigen", onShowAll)
    }
}

@Composable
private fun EndPage(filter: FeedFilter, onRestart: () -> Unit, onOpenCategories: () -> Unit) {
    CenteredMessage(
        title = "Das war's für „${filter.title}“",
        text = "Du hast alle Zitate dieser Auswahl gesehen.",
    ) {
        ThemedOutlinedButton("Nochmal von vorn", onRestart)
        Spacer(Modifier.height(8.dp))
        ThemedOutlinedButton("Andere Kategorie wählen", onOpenCategories)
    }
}

/** Button in der Textfarbe des Themes (die Material-Standardfarbe wäre auf manchen Hintergründen unlesbar). */
@Composable
private fun ThemedOutlinedButton(text: String, onClick: () -> Unit) {
    val color = LocalContentColor.current
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
    ) { Text(text) }
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
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {}, onOpenOwnQuotes = {},
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
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {}, onOpenOwnQuotes = {},
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
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {}, onOpenOwnQuotes = {},
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
            onOpenCategories = {}, onOpenThemes = {}, onOpenSettings = {}, onOpenOwnQuotes = {},
        )
    }
}
