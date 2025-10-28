package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val KEY_WELCOME_DISMISSED = booleanPreferencesKey("key_welcome_dismissed")

internal class DismissWelcomeUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    suspend fun dismissWelcome() {
        mainDataStore.edit { prefs ->
            prefs[KEY_WELCOME_DISMISSED] = true
        }
    }

    suspend fun isWelcomeDismissed(): Boolean {
        val prefs = mainDataStore.data.first()
        return prefs[KEY_WELCOME_DISMISSED] ?: false
    }
}
