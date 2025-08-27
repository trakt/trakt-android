package tv.trakt.trakt.core.home.sections.activity.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityFilter.SOCIAL

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_activity_filter")

internal class GetActivityFilterUseCase(
    private val homeDataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): HomeActivityFilter {
        val storedFilter = homeDataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            HomeActivityFilter.valueOf(it)
        } ?: SOCIAL
    }

    suspend fun setFilter(filter: HomeActivityFilter) {
        homeDataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
