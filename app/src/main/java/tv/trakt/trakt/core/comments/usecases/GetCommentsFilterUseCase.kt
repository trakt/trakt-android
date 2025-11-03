package tv.trakt.trakt.core.comments.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.comments.model.CommentsFilter

private val KEY_COMMENTS_FILTER = stringPreferencesKey("key_comments_filter")

internal class GetCommentsFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): CommentsFilter {
        val storedFilter = dataStore.data.first()[KEY_COMMENTS_FILTER]
        return storedFilter?.let {
            CommentsFilter.valueOf(it)
        } ?: CommentsFilter.POPULAR
    }

    suspend fun setFilter(filter: CommentsFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_COMMENTS_FILTER] = filter.name
            }
        }
    }
}
