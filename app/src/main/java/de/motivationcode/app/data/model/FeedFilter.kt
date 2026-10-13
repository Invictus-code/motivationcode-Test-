package de.motivationcode.app.data.model

/** Was im Feed angezeigt wird: alle Zitate, nur Favoriten oder eine Kategorie. */
sealed interface FeedFilter {
    data object All : FeedFilter
    data object Favorites : FeedFilter
    data object Own : FeedFilter
    data class ByCategory(val category: Category) : FeedFilter

    val title: String
        get() = when (this) {
            All -> "Alle Zitate"
            Favorites -> "Favoriten"
            Own -> "Meine Zitate"
            is ByCategory -> category.title
        }

    /** Kompakte Form zum Speichern in DataStore. */
    fun serialize(): String = when (this) {
        All -> "all"
        Favorites -> "favorites"
        Own -> "own"
        is ByCategory -> "category:${category.id}"
    }

    companion object {
        fun deserialize(value: String?): FeedFilter = when {
            value == "favorites" -> Favorites
            value == "own" -> Own
            value != null && value.startsWith("category:") ->
                Category.fromId(value.removePrefix("category:"))?.let(::ByCategory) ?: All
            else -> All
        }
    }
}
