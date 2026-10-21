package tv.trakt.trakt.core.calendar.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.core.calendar.model.CalendarView

private val KEY_CALENDAR_VIEW = stringPreferencesKey("key_calendar_view")

internal class GetCalendarViewUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeView(): Flow<CalendarView> {
        return dataStore.data
            .map { preferences ->
                val storedView = preferences[KEY_CALENDAR_VIEW]
                CalendarView.entries.firstOrNull { it.name == storedView } ?: CalendarView.Weekly
            }
            .distinctUntilChanged()
    }

    suspend fun setView(view: CalendarView) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_CALENDAR_VIEW] = view.name
            }
        }
    }
}
