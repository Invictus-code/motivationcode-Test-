package de.motivationcode.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.motivationcode.app.ui.categories.CategoriesRoute
import de.motivationcode.app.ui.feed.FeedRoute
import de.motivationcode.app.ui.settings.SettingsRoute
import de.motivationcode.app.ui.themes.ThemesRoute
import kotlinx.serialization.Serializable

@Serializable
data object FeedKey : NavKey

@Serializable
data object CategoriesKey : NavKey

@Serializable
data object ThemesKey : NavKey

@Serializable
data object SettingsKey : NavKey

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(FeedKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<FeedKey> {
                FeedRoute(
                    onOpenCategories = { backStack.open(CategoriesKey) },
                    onOpenThemes = { backStack.open(ThemesKey) },
                    onOpenSettings = { backStack.open(SettingsKey) },
                )
            }
            entry<CategoriesKey> { CategoriesRoute(onBack = { backStack.goBack() }) }
            entry<ThemesKey> { ThemesRoute(onBack = { backStack.goBack() }) }
            entry<SettingsKey> { SettingsRoute(onBack = { backStack.goBack() }) }
        },
    )
}

/** Verhindert doppelte Einträge, wenn man schnell zweimal tippt. */
private fun NavBackStack<NavKey>.open(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

/** Der Feed bleibt immer als unterster Eintrag stehen. */
private fun NavBackStack<NavKey>.goBack() {
    if (size > 1) removeAt(lastIndex)
}
