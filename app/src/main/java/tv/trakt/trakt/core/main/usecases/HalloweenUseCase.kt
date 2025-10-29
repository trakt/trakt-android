package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HALLOWEEN_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HALLOWEEN_HEADER
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_HALLOWEEN_SUBHEADER

/**
 * Preference key to store whether the user has enabled or disabled the Halloween theme for 2025.
 * Default is true (enabled).
 */
internal val KEY_HALLOWEEN_USER_ENABLED = booleanPreferencesKey("key_halloween_user_enabled_2025")

/**
 * Use case to manage the Halloween theme preference.
 */
internal class HalloweenUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig

    suspend fun toggleUserEnabled(enabled: Boolean) {
        mainDataStore.edit { prefs ->
            prefs[KEY_HALLOWEEN_USER_ENABLED] = enabled
        }
    }

    suspend fun getConfig(): HalloweenConfig {
        val isUserEnabled = mainDataStore.data.first()[KEY_HALLOWEEN_USER_ENABLED] ?: true
        val isConfigEnabled = remoteConfig.getBoolean(MOBILE_HALLOWEEN_ENABLED)
        return HalloweenConfig(
            visible = isConfigEnabled,
            enabled = isUserEnabled && isConfigEnabled,
            header = remoteConfig.getString(MOBILE_HALLOWEEN_HEADER),
            subheader = remoteConfig.getString(MOBILE_HALLOWEEN_SUBHEADER),
        )
    }

    data class HalloweenConfig(
        val visible: Boolean,
        val enabled: Boolean,
        val header: String? = null,
        val subheader: String? = null,
    )
}
