@file:OptIn(ExperimentalMaterial3Api::class)

package de.motivationcode.app.ui.categories

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.motivationcode.app.data.model.Category
import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.ui.components.SystemBarsAppearance
import de.motivationcode.app.ui.theme.TagesmotivationTheme

@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesScreen(
        uiState = uiState,
        onSelect = { filter -> viewModel.select(filter, onSaved = onBack) },
        onBack = onBack,
    )
}

@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onSelect: (FeedFilter) -> Unit,
    onBack: () -> Unit,
) {
    SystemBarsAppearance(lightBackground = !isSystemInDarkTheme())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kategorien") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                CategoriesUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                CategoriesUiState.Error -> Text(
                    "Kategorien konnten nicht geladen werden.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
                is CategoriesUiState.Content -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.items, key = { it.filter.serialize() }) { item ->
                        CategoryCard(item = item, onClick = { onSelect(item.filter) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(item: CategoryItem, onClick: () -> Unit) {
    val colors = if (item.selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(
        onClick = onClick,
        colors = colors,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .semantics { selected = item.selected },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.filter.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (item.count == 1) "1 Zitat" else "${item.count} Zitate",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (item.selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Ausgewählt")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CategoriesPreview() {
    TagesmotivationTheme {
        CategoriesScreen(
            uiState = CategoriesUiState.Content(
                listOf(
                    CategoryItem(FeedFilter.All, 85, selected = true),
                    CategoryItem(FeedFilter.Favorites, 0, selected = false),
                    CategoryItem(FeedFilter.ByCategory(Category.MUT), 21, selected = false),
                    CategoryItem(FeedFilter.ByCategory(Category.GLUECK), 9, selected = false),
                ),
            ),
            onSelect = {},
            onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CategoriesLoadingPreview() {
    TagesmotivationTheme {
        CategoriesScreen(uiState = CategoriesUiState.Loading, onSelect = {}, onBack = {})
    }
}
