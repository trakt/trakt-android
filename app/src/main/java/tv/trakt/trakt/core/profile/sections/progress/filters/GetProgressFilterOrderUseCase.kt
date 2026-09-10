package tv.trakt.trakt.core.profile.sections.progress.filters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter

private val KEY_PROGRESS_FILTER_ORDER = stringPreferencesKey("key_profile_progress_filter_order")

private const val SEPARATOR = ","

internal class GetProgressFilterOrderUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getOrder(): ImmutableList<ProgressFilter> {
        val storedOrder = dataStore.data.first()[KEY_PROGRESS_FILTER_ORDER]
        return sanitize(storedOrder)
    }

    suspend fun setOrder(order: List<ProgressFilter>) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_PROGRESS_FILTER_ORDER] = order.joinToString(SEPARATOR) { filter -> filter.name }
            }
        }
    }

    /**
     * Drops names that no longer resolve and appends, in declaration order, any filter the stored
     * value does not mention. A preference written by an older build therefore survives the enum
     * gaining or losing an entry instead of hiding a chip.
     */
    private fun sanitize(storedOrder: String?): ImmutableList<ProgressFilter> {
        val stored = storedOrder
            ?.split(SEPARATOR)
            ?.mapNotNull { name -> runCatching { ProgressFilter.valueOf(name) }.getOrNull() }
            ?.distinct()
            .orEmpty()

        return (stored + ProgressFilter.entries.filterNot { it in stored }).toImmutableList()
    }
}
