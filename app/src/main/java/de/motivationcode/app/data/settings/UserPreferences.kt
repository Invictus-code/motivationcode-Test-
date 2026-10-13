package de.motivationcode.app.data.settings

import de.motivationcode.app.data.model.FeedFilter
import de.motivationcode.app.data.themes.QuoteThemes
import java.time.LocalTime

data class UserPreferences(
    val favoriteIds: Set<String> = emptySet(),
    val feedFilter: FeedFilter = FeedFilter.All,
    val themeId: String = QuoteThemes.default.id,
    val reminders: ReminderSettings = ReminderSettings(),
)

data class ReminderSettings(
    val enabled: Boolean = false,
    val perDay: Int = 3,
    val start: LocalTime = LocalTime.of(9, 0),
    val end: LocalTime = LocalTime.of(21, 0),
) {
    companion object {
        const val MIN_PER_DAY = 1
        const val MAX_PER_DAY = 10
    }
}
