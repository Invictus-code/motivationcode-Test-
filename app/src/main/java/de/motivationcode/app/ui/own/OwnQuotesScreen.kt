@file:OptIn(ExperimentalMaterial3Api::class)

package de.motivationcode.app.ui.own

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.own.normalizeQuoteText
import de.motivationcode.app.ui.components.SystemBarsAppearance
import de.motivationcode.app.ui.theme.TagesmotivationTheme
import kotlinx.coroutines.launch

@Composable
fun OwnQuotesRoute(
    onBack: () -> Unit,
    viewModel: OwnQuotesViewModel = viewModel(factory = OwnQuotesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OwnQuotesScreen(
        uiState = uiState,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onRestore = viewModel::restore,
        onBack = onBack,
    )
}

@Composable
fun OwnQuotesScreen(
    uiState: OwnQuotesUiState,
    onSave: (id: String?, text: String, author: String) -> Unit,
    onDelete: (OwnQuote) -> Unit,
    onRestore: (OwnQuote) -> Unit,
    onBack: () -> Unit,
) {
    SystemBarsAppearance(lightBackground = !isSystemInDarkTheme())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Entwurf im Editor, übersteht auch das Drehen des Handys.
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var draftText by rememberSaveable { mutableStateOf("") }
    var draftAuthor by rememberSaveable { mutableStateOf("") }

    fun openEditor(quote: OwnQuote?) {
        editingId = quote?.id
        draftText = quote?.text.orEmpty()
        draftAuthor = quote?.author.orEmpty()
        editorOpen = true
    }

    fun deleteWithUndo(quote: OwnQuote) {
        onDelete(quote)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "Zitat gelöscht",
                actionLabel = "Rückgängig",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onRestore(quote)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eigene Zitate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState is OwnQuotesUiState.Content) {
                ExtendedFloatingActionButton(
                    onClick = { openEditor(null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Zitat hinzufügen") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                OwnQuotesUiState.Loading -> CircularProgressIndicator(Modifier.padding(32.dp))
                OwnQuotesUiState.Error -> Text(
                    text = "Deine Zitate konnten nicht geladen werden.",
                    modifier = Modifier.padding(24.dp),
                )
                is OwnQuotesUiState.Content -> if (uiState.quotes.isEmpty()) {
                    EmptyOwnQuotes()
                } else {
                    LazyColumn(
                        modifier = Modifier.widthIn(max = 640.dp),
                        // Unten Platz lassen, damit der Button die letzte Karte nicht verdeckt.
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.quotes, key = { it.id }) { quote ->
                            OwnQuoteCard(
                                quote = quote,
                                onEdit = { openEditor(quote) },
                                onDelete = { deleteWithUndo(quote) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        QuoteEditorDialog(
            isNew = editingId == null,
            text = draftText,
            onTextChange = { draftText = it.take(OwnQuote.MAX_TEXT_LENGTH) },
            author = draftAuthor,
            onAuthorChange = { draftAuthor = it.take(OwnQuote.MAX_AUTHOR_LENGTH) },
            onSave = {
                onSave(editingId, draftText, draftAuthor)
                editorOpen = false
            },
            onDismiss = { editorOpen = false },
        )
    }
}

@Composable
private fun EmptyOwnQuotes() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(max = 480.dp)
            .padding(32.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "Noch keine eigenen Zitate",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sammle hier Sprüche, die dich motivieren. Sie erscheinen dann im Feed, im Widget " +
                "und in den Erinnerungen.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OwnQuoteCard(
    quote: OwnQuote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onEdit, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "„${quote.text}“",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                quote.author?.let {
                    Text(
                        text = "— $it",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Zitat löschen")
            }
        }
    }
}

@Composable
private fun QuoteEditorDialog(
    isNew: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    author: String,
    onAuthorChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Neues Zitat" else "Zitat bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("Zitat") },
                    minLines = 3,
                    maxLines = 6,
                    supportingText = {
                        Text(
                            text = "${text.length} / ${OwnQuote.MAX_TEXT_LENGTH}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = onAuthorChange,
                    label = { Text("Autor (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = normalizeQuoteText(text).isNotEmpty()) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

private val previewQuotes = listOf(
    OwnQuote("own-1", "Heute ist ein guter Tag, um anzufangen.", author = null, createdAt = 2),
    OwnQuote("own-2", "Kleine Schritte sind auch Schritte.", author = "Oma Hilde", createdAt = 1),
)

@Preview(showSystemUi = true)
@Composable
private fun OwnQuotesPreview() {
    TagesmotivationTheme {
        OwnQuotesScreen(OwnQuotesUiState.Content(previewQuotes), { _, _, _ -> }, {}, {}, {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OwnQuotesEmptyPreview() {
    TagesmotivationTheme {
        OwnQuotesScreen(OwnQuotesUiState.Content(emptyList()), { _, _, _ -> }, {}, {}, {})
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OwnQuotesLoadingPreview() {
    TagesmotivationTheme {
        OwnQuotesScreen(OwnQuotesUiState.Loading, { _, _, _ -> }, {}, {}, {})
    }
}
