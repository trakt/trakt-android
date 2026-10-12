package tv.trakt.trakt.core.main.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.YIR_2026_BANNER_ENABLED
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.YIR_2026_URL

private val KEY_YIR_2026_BANNER_DISMISSED = booleanPreferencesKey("key_yir_2026_banner_dismissed_0")

internal class YearInReviewBannerUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig

    suspend fun loadBannerUrl(): String? {
        if (!remoteConfig.getBoolean(YIR_2026_BANNER_ENABLED)) {
            return null
        }

        val url = remoteConfig.getString(YIR_2026_URL)
        if (url.isBlank()) {
            return null
        }

        val dismissed = mainDataStore.data.first()[KEY_YIR_2026_BANNER_DISMISSED] ?: false
        if (dismissed) {
            return null
        }

        return url
    }

    suspend fun dismissBanner() {
        mainDataStore.edit { prefs ->
            prefs[KEY_YIR_2026_BANNER_DISMISSED] = true
        }
    }
}
