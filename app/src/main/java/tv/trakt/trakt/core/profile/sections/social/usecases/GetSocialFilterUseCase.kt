package tv.trakt.trakt.core.profile.sections.social.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter

private val KEY_ACTIVITY_FILTER = stringPreferencesKey("key_profile_social_filter")

internal class GetSocialFilterUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getFilter(): SocialFilter {
        val storedFilter = dataStore.data.first()[KEY_ACTIVITY_FILTER]
        return storedFilter?.let {
            SocialFilter.valueOf(it)
        } ?: SocialFilter.FOLLOWING
    }

    suspend fun setFilter(filter: SocialFilter) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_ACTIVITY_FILTER] = filter.name
            }
        }
    }
}
