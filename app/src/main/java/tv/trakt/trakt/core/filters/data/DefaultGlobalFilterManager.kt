package tv.trakt.trakt.core.filters.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode

private val KEY_FILTERS = stringPreferencesKey("key_global_filters")
private val KEY_FILTERS_MODE = stringPreferencesKey("key_global_filters_mode")

internal class DefaultGlobalFilterManager(
    scope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
) : GlobalFilterManager {
    private val modeState = dataStore.data
        .map { prefs ->
            val value = prefs[KEY_FILTERS_MODE] ?: return@map GlobalFilterMode.Simple
            GlobalFilterMode.entries.find { it.name == value } ?: GlobalFilterMode.Simple
        }
        .stateIn(scope, SharingStarted.Eagerly, GlobalFilterMode.Simple)

    private val filtersState = dataStore.data
        .map { prefs ->
            val json = prefs[KEY_FILTERS] ?: return@map GlobalFilter.Default
            Json.decodeFromString(json)
        }
        .stateIn(scope, SharingStarted.Eagerly, GlobalFilter.Default)

    private val filtersShared = filtersState
        .shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override suspend fun setFilter(filter: GlobalFilter) {
        dataStore.edit { it[KEY_FILTERS] = Json.encodeToString(filter) }
    }

    override suspend fun setMode(mode: GlobalFilterMode) {
        dataStore.edit { it[KEY_FILTERS_MODE] = mode.name }
    }

    override fun getFilter(): GlobalFilter = filtersState.value

    override fun getMode(): GlobalFilterMode = modeState.value

    override fun observeFilter(): Flow<GlobalFilter> = filtersShared
}
