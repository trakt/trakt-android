package tv.trakt.trakt.core.filters.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode

private val KEY_FILTERS = stringPreferencesKey("key_global_filters")
private val KEY_FILTERS_MODE = stringPreferencesKey("key_global_filters_mode")

internal class DefaultGlobalFilterManager(
    private val dataStore: DataStore<Preferences>,
) : GlobalFilterManager {
    private var currentMode: GlobalFilterMode = runBlocking {
        val data = dataStore.data.first()
        val modeValue = data[KEY_FILTERS_MODE] ?: return@runBlocking GlobalFilterMode.Simple

        GlobalFilterMode.entries
            .find { it.name == modeValue } ?: GlobalFilterMode.Simple
    }

    private var currentFilters: GlobalFilter = runBlocking {
        val data = dataStore.data.first()
        val filterJson = data[KEY_FILTERS]
            ?: return@runBlocking GlobalFilter.Default

        Json.decodeFromString(filterJson)
    }

    private val currentFiltersFlow = MutableSharedFlow<GlobalFilter>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setFilter(filter: GlobalFilter) {
        currentFilters = filter
        currentFiltersFlow.emit(filter)

        dataStore.edit {
            it[KEY_FILTERS] = Json.encodeToString(filter)
        }
    }

    override fun setMode(mode: GlobalFilterMode) {
        currentMode = mode
        runBlocking {
            dataStore.edit {
                it[KEY_FILTERS_MODE] = mode.name
            }
        }
    }

    override fun getFilter(): GlobalFilter = currentFilters

    override fun getMode(): GlobalFilterMode = currentMode

    override fun observeFilter(): Flow<GlobalFilter> = currentFiltersFlow
}
