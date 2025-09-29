package tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.lists.model.ListsMediaFilter
import tv.trakt.trakt.core.lists.model.ListsMediaFilter.MEDIA

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_activity_watchlist_filter")

internal class GetWatchlistFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): ListsMediaFilter {
        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            ListsMediaFilter.valueOf(it)
        } ?: MEDIA
    }

    suspend fun setFilter(filter: ListsMediaFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
