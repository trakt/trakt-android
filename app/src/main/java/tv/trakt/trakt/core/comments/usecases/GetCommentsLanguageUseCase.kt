package tv.trakt.trakt.core.comments.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

private val KEY_COMMENTS_LANGUAGE = stringPreferencesKey("key_comments_language")

/**
 * Comments language filter, shared by movie, show and episode comments. A null language means all
 * languages.
 */
internal class GetCommentsLanguageUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getLanguage(): String? {
        return dataStore.data.first()[KEY_COMMENTS_LANGUAGE]
    }

    suspend fun setLanguage(language: String?) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                when (language) {
                    null -> remove(KEY_COMMENTS_LANGUAGE)
                    else -> this[KEY_COMMENTS_LANGUAGE] = language
                }
            }
        }
    }
}
