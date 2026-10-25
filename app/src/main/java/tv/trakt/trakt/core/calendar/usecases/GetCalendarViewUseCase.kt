package tv.trakt.trakt.core.calendar.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.calendar.model.CalendarView

private val KEY_CALENDAR_VIEW = stringPreferencesKey("key_calendar_view")
private val KEY_RELEASES_VIEW = stringPreferencesKey("key_releases_view")
private val KEY_VIEW_HINT_SEEN = booleanPreferencesKey("key_view_hint_seen")

internal class GetCalendarViewUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeView(source: CalendarSource): Flow<CalendarView> {
        return dataStore.data
            .map { preferences ->
                val storedView = preferences[source.viewKey]
                CalendarView.entries.firstOrNull { it.name == storedView } ?: CalendarView.Weekly
            }
            .distinctUntilChanged()
    }

    suspend fun setView(
        source: CalendarSource,
        view: CalendarView,
    ) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[source.viewKey] = view.name
            }
        }
    }

    fun observeViewHint(): Flow<Boolean> {
        return dataStore.data
            .map { preferences -> preferences[KEY_VIEW_HINT_SEEN] != true }
            .distinctUntilChanged()
    }

    suspend fun dismissViewHint() {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_VIEW_HINT_SEEN] = true
            }
        }
    }
}

private val CalendarSource.viewKey: Preferences.Key<String>
    get() = when (this) {
        CalendarSource.Personal -> KEY_CALENDAR_VIEW
        CalendarSource.Releases -> KEY_RELEASES_VIEW
    }
