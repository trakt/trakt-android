package tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistFilter.MEDIA

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_activity_watchlist_filter")

internal class GetWatchlistFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): WatchlistFilter {
        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            WatchlistFilter.valueOf(it)
        } ?: MEDIA
    }

    suspend fun setFilter(filter: WatchlistFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
