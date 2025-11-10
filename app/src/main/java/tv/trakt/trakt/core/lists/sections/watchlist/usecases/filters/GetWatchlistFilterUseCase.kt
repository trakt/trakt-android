package tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters

import androidx.datastore.preferences.core.stringPreferencesKey

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_activity_watchlist_filter")

// internal class GetWatchlistFilterUseCase(
//    private val dataStore: DataStore<Preferences>,
// ) {
//    suspend fun getFilter(): MediaMode {
//        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
//        return storedFilter?.let {
//            MediaMode.valueOf(it)
//        } ?: MediaMode.MEDIA
//    }
//
//    suspend fun setFilter(filter: MediaMode) {
//        dataStore.updateData {
//            it.toMutablePreferences().apply {
//                this[KEY_ACTIVITY_FILTER] = filter.name
//            }
//        }
//    }
// }
