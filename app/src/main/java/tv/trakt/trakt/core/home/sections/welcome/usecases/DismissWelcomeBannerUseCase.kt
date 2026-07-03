package tv.trakt.trakt.core.home.sections.welcome.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val KEY_WELCOME_BANNER_DISMISSED = booleanPreferencesKey("key_welcome_banner_dismissed")

internal class DismissWelcomeBannerUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    suspend fun dismissWelcomeBanner() {
        mainDataStore.edit { prefs ->
            prefs[KEY_WELCOME_BANNER_DISMISSED] = true
        }
    }

    suspend fun isDismissed(): Boolean {
        val prefs = mainDataStore.data.first()
        return prefs[KEY_WELCOME_BANNER_DISMISSED] ?: false
    }
}
