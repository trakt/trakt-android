package tv.trakt.trakt.core.profile.sections.activity.usecases.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_profile_activity_filters")

internal class GetActivityFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): ProfileActivityFilter {
        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            ProfileActivityFilter.valueOf(it)
        } ?: ProfileActivityFilter.Ratings
    }

    suspend fun setFilter(filter: ProfileActivityFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
