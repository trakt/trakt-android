package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.firebase.FirebaseConfig
import tv.trakt.trakt.common.model.WhatsNew

internal class LoadWhatsNewUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig

    suspend fun getWhatsNew(): WhatsNew? {
        val configWhatsNew = remoteConfig.getString(FirebaseConfig.MOBILE_WHATS_NEW)

        // If the config is blank, there's no What's New to show
        if (configWhatsNew.isBlank()) {
            return null
        }

        val whatsNew = try {
            Json.decodeFromString<WhatsNew>(configWhatsNew)
        } catch (error: Exception) {
            Timber.recordError(error)
            return null
        }

        // If the version in the config doesn't match the app version, don't show it
        if (!BuildConfig.VERSION_NAME.equals(whatsNew.versionName, ignoreCase = true)) {
            return null
        }

        // If the version code in the config is greater than the app version code, don't show it
        if (BuildConfig.VERSION_CODE < whatsNew.versionCode) {
            return null
        }

        val dismissedWhatsNew = dataStore.data.firstOrNull()

        // Initial install, no preferences saved yet, so nothing has been dismissed.
        if (dismissedWhatsNew?.asMap()?.isEmpty() == true) {
            dismissWhatsNew(whatsNew.id)
            return null
        }

        // If the user has dismissed this version of What's New, don't show it again
        val currentId = dismissedWhatsNew?.get(getPreferenceKey())
        if (currentId != null && currentId >= whatsNew.id) {
            return null
        }

        return whatsNew
    }

    suspend fun dismissWhatsNew(id: Int) {
        dataStore.edit { prefs ->
            prefs[getPreferenceKey()] = id
        }
    }

    private fun getPreferenceKey() = intPreferencesKey("key_dismiss_whats_new")
}
