package tv.trakt.trakt.core.calendar.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay

private val KEY_CALENDAR_DISPLAY = stringPreferencesKey("key_calendar_day_display")

internal class GetCalendarDisplayUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getDisplay(): CalendarDayDisplay {
        val storedDisplay = dataStore.data.first()[KEY_CALENDAR_DISPLAY]
        return storedDisplay?.let {
            runCatching { CalendarDayDisplay.valueOf(it) }.getOrNull()
        } ?: CalendarDayDisplay.Posters
    }

    suspend fun setDisplay(display: CalendarDayDisplay) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_CALENDAR_DISPLAY] = display.name
            }
        }
    }
}
