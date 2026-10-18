package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.common.firebase.FirebaseConfig
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.model.WhatsNew

internal class LoadWhatsNewUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getWhatsNew(): WhatsNew? {
        val configWhatsNew = remoteConfig.getString(FirebaseConfig.MOBILE_WHATS_NEW)
        if (configWhatsNew.isBlank()) {
            // If the config is blank, there's no What's New to show
            return null
        }

        val releases = try {
            json.decodeFromString<List<WhatsNew>>(configWhatsNew)
        } catch (error: Exception) {
            Timber.recordError(error)
            return null
        }

        // Patch versions are ignored so notes only change on a new major or minor release
        val installedLine = releaseLine(BuildConfig.VERSION_NAME)
        val release = releases
            .filter { releaseLine(it.version) == installedLine }
            .maxByOrNull { it.id }
            ?: return null

        val prefs = dataStore.data.firstOrNull()
        val dismissedId = prefs?.get(dismissedKey)

        // Initial install: there is no upgrade to report, so mark the current release as seen
        // and show nothing this run.
        if (prefs?.get(seededKey) != true && dismissedId == null) {
            dismissWhatsNew(release.id)
            return null
        }

        // If the user has dismissed this release of What's New, don't show it again
        if (dismissedId != null && dismissedId >= release.id) {
            return null
        }

        return release
    }

    suspend fun dismissWhatsNew(id: Int) {
        dataStore.edit { prefs ->
            prefs[dismissedKey] = id
            prefs[seededKey] = true
        }
    }

    private fun releaseLine(version: String): String {
        return version.split(".").take(2).joinToString(".")
    }

    private companion object {
        val dismissedKey = intPreferencesKey("key_dismiss_whats_new")
        val seededKey = booleanPreferencesKey("key_seeded_whats_new")
    }
}
