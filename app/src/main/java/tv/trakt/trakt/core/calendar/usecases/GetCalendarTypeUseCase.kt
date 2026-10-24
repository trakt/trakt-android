package tv.trakt.trakt.core.calendar.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.calendar.model.CalendarSource
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.discover.sections.releases.usecases.GetReleasesTypeUseCase

private val KEY_CALENDAR_TYPE = stringPreferencesKey("key_calendar_release_type")

internal class GetCalendarTypeUseCase(
    private val dataStore: DataStore<Preferences>,
    private val getReleasesTypeUseCase: GetReleasesTypeUseCase,
) {
    suspend fun getType(source: CalendarSource): ReleaseType {
        if (source == CalendarSource.Releases) {
            return getReleasesTypeUseCase.getType()
        }

        val storedType = dataStore.data.first()[KEY_CALENDAR_TYPE]
        return storedType?.let {
            runCatching { ReleaseType.valueOf(it) }.getOrNull()
        } ?: ReleaseType.All
    }

    suspend fun setType(type: ReleaseType) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_CALENDAR_TYPE] = type.name
            }
        }
    }
}
