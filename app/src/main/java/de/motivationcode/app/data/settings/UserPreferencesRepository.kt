package de.motivationcode.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import de.motivationcode.app.data.model.FeedFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun toggleFavorite(quoteId: String)
    suspend fun setFeedFilter(filter: FeedFilter)
    suspend fun setTheme(themeId: String)
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun setRemindersPerDay(perDay: Int)
    suspend fun setReminderStart(time: LocalTime)
    suspend fun setReminderEnd(time: LocalTime)
}

class DataStoreUserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toUserPreferences() }

    override suspend fun toggleFavorite(quoteId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES].orEmpty()
            prefs[Keys.FAVORITES] = if (quoteId in current) current - quoteId else current + quoteId
        }
    }

    override suspend fun setFeedFilter(filter: FeedFilter) {
        dataStore.edit { it[Keys.FEED_FILTER] = filter.serialize() }
    }

    override suspend fun setTheme(themeId: String) {
        dataStore.edit { it[Keys.THEME] = themeId }
    }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    override suspend fun setRemindersPerDay(perDay: Int) {
        val clamped = perDay.coerceIn(ReminderSettings.MIN_PER_DAY, ReminderSettings.MAX_PER_DAY)
        dataStore.edit { it[Keys.REMINDERS_PER_DAY] = clamped }
    }

    override suspend fun setReminderStart(time: LocalTime) {
        dataStore.edit { it[Keys.REMINDER_START_MINUTES] = time.toMinuteOfDay() }
    }

    override suspend fun setReminderEnd(time: LocalTime) {
        dataStore.edit { it[Keys.REMINDER_END_MINUTES] = time.toMinuteOfDay() }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences()
        return UserPreferences(
            favoriteIds = this[Keys.FAVORITES].orEmpty(),
            feedFilter = FeedFilter.deserialize(this[Keys.FEED_FILTER]),
            themeId = this[Keys.THEME] ?: defaults.themeId,
            reminders = ReminderSettings(
                enabled = this[Keys.REMINDERS_ENABLED] ?: defaults.reminders.enabled,
                perDay = this[Keys.REMINDERS_PER_DAY] ?: defaults.reminders.perDay,
                start = this[Keys.REMINDER_START_MINUTES]?.toLocalTime() ?: defaults.reminders.start,
                end = this[Keys.REMINDER_END_MINUTES]?.toLocalTime() ?: defaults.reminders.end,
            ),
        )
    }

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorite_ids")
        val FEED_FILTER = stringPreferencesKey("feed_filter")
        val THEME = stringPreferencesKey("theme_id")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDERS_PER_DAY = intPreferencesKey("reminders_per_day")
        val REMINDER_START_MINUTES = intPreferencesKey("reminder_start_minutes")
        val REMINDER_END_MINUTES = intPreferencesKey("reminder_end_minutes")
    }
}

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute
private fun Int.toLocalTime(): LocalTime = LocalTime.of((this / 60) % 24, this % 60)
