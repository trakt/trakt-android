package tv.trakt.trakt.core.comments.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.core.comments.model.appCommentsLanguage

private val KEY_COMMENTS_LANGUAGE = stringPreferencesKey("key_comments_language")

/**
 * Persisted when the user explicitly picks "All languages", so an absent key keeps meaning "never
 * chosen" and can default to the current app language. Not a valid language code, so it can never
 * collide with a stored one.
 */
private const val ALL_LANGUAGES = "__all__"

internal class GetCommentsLanguageUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getLanguage(): String? {
        val stored = dataStore.data.first()[KEY_COMMENTS_LANGUAGE] ?: return appCommentsLanguage()
        if (stored == ALL_LANGUAGES) return null

        return stored
    }

    suspend fun setLanguage(language: String?) {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_COMMENTS_LANGUAGE] = language ?: ALL_LANGUAGES
            }
        }
    }
}
