package tv.trakt.trakt.core.calendar.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.calendar.model.CalendarView

private val KEY_CALENDAR_VIEW = stringPreferencesKey("key_calendar_view")

internal class GetCalendarViewUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getView(): CalendarView {
        val storedView = dataStore.data.first()[KEY_CALENDAR_VIEW]
        return storedView?.let {
            runCatching { CalendarView.valueOf(it) }.getOrNull()
        } ?: CalendarView.Weekly
    }

    suspend fun setView(view: CalendarView) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_CALENDAR_VIEW] = view.name
            }
        }
    }
}
