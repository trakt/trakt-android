package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val KEY_PAYWALL_DISMISSED = booleanPreferencesKey("key_paywall_dismissed")

internal class DismissPaywallUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    suspend fun dismissPaywall() {
        mainDataStore.edit { prefs ->
            prefs[KEY_PAYWALL_DISMISSED] = true
        }
    }

    suspend fun isPaywallDismissed(): Boolean {
        val prefs = mainDataStore.data.first()
        return prefs[KEY_PAYWALL_DISMISSED] ?: false
    }
}
