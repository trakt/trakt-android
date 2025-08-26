package tv.trakt.trakt.app.core.inappreview.usecases

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.first

private val KEY_APP_REVIEW_COUNT = longPreferencesKey("key_app_review_count")

internal class RequestAppReviewUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    /**
     * Returns true if the app should request an in-app review from the user.
     */
    suspend fun shouldRequest(): Boolean {
        val remoteConfig = Firebase.remoteConfig

        val data = mainDataStore.data.first()
        val count = data[KEY_APP_REVIEW_COUNT] ?: 0L

        val requestCounts = longArrayOf(
            remoteConfig.getLong("in_app_review_count_1"),
            remoteConfig.getLong("in_app_review_count_2"),
            remoteConfig.getLong("in_app_review_count_3"),
        )

        return (count in requestCounts).also {
            Log.d(
                "RequestAppReview",
                "shouldRequest: $it (count = $count) counts = ${requestCounts.joinToString(", ")}",
            )
        }
    }

    suspend fun incrementCount() {
        val data = mainDataStore.data.first()
        val appCount = data[KEY_APP_REVIEW_COUNT] ?: 0L

        mainDataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_APP_REVIEW_COUNT] = appCount + 1
            }
        }
    }
}
