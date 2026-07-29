package tv.trakt.trakt.core.lists.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.lists.sections.personal.model.PersonalListType

private val KEY_LISTS_FILTER = stringPreferencesKey("key_lists_filters")

internal class GetListsFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): PersonalListType {
        val storedFilter = dataStore.data.first()[KEY_LISTS_FILTER]
        return storedFilter?.let {
            PersonalListType.valueOf(it)
        } ?: PersonalListType.Personal
    }

    suspend fun setFilter(filter: PersonalListType) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_LISTS_FILTER] = filter.name
            }
        }
    }
}
