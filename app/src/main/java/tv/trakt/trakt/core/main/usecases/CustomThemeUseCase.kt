package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_CUSTOM_THEME_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_CUSTOM_THEME_JSON
import tv.trakt.trakt.ui.theme.model.CustomTheme

internal val KEY_CUSTOM_THEME_USER_ENABLED = booleanPreferencesKey("key_custom_theme_user_enabled")

internal class CustomThemeUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig
    private val json = Json {
        explicitNulls = false
    }

    suspend fun toggleUserEnabled(enabled: Boolean) {
        mainDataStore.edit { prefs ->
            prefs[KEY_CUSTOM_THEME_USER_ENABLED] = enabled
        }
    }

    suspend fun getConfig(): CustomThemeConfig {
        val isUserEnabled = mainDataStore.data.first()[KEY_CUSTOM_THEME_USER_ENABLED] ?: true
        val isConfigEnabled = remoteConfig.getBoolean(MOBILE_CUSTOM_THEME_ENABLED)

        val configThemeJson = remoteConfig.getString(MOBILE_CUSTOM_THEME_JSON)
        val configTheme: CustomTheme? = try {
            if (configThemeJson.isNotBlank()) {
                json.decodeFromString(configThemeJson)
            } else {
                null
            }
        } catch (error: Exception) {
            Timber.recordError(error)
            null
        }

        return CustomThemeConfig(
            visible = isConfigEnabled,
            enabled = isUserEnabled && isConfigEnabled,
            theme = configTheme,
        )
    }

    data class CustomThemeConfig(
        val visible: Boolean,
        val enabled: Boolean,
        val theme: CustomTheme? = null,
    )
}
