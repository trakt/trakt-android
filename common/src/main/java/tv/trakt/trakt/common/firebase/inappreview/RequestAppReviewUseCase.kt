package tv.trakt.trakt.common.firebase.inappreview

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val KEY_APP_REVIEW_COUNT = longPreferencesKey("key_app_review_count")

class RequestAppReviewUseCase(
    private val mainDataStore: DataStore<Preferences>,
) {
    /**
     * Returns true if the app should request an in-app review from the user.
     */
    suspend fun shouldRequest(): Boolean {
        val data = mainDataStore.data.first()
        val count = data[KEY_APP_REVIEW_COUNT] ?: 0L

        val requestCounts = with(Firebase.remoteConfig) {
            longArrayOf(
                getLong("in_app_review_count_1"),
                getLong("in_app_review_count_2"),
                getLong("in_app_review_count_3"),
            )
        }

        return (count in requestCounts).also {
            Timber.d("shouldRequest: $it (count = $count) counts = ${requestCounts.joinToString(", ")}")
        }
    }

    /**
     * Increments the count of how many times the app has requested an in-app review.
     */
    suspend fun incrementCount() {
        val data = mainDataStore.data.first()
        val appCount = data[KEY_APP_REVIEW_COUNT] ?: 0L

        mainDataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_APP_REVIEW_COUNT] = appCount + 1
            }
        }

        Timber.d("incrementCount: count = ${appCount + 1}")
    }

    fun observeCount(): Flow<Long?> {
        return mainDataStore.data
            .map { it[KEY_APP_REVIEW_COUNT] }
            .drop(1)
    }

    suspend fun clear() {
        mainDataStore.updateData {
            it.toMutablePreferences().apply {
                this[KEY_APP_REVIEW_COUNT] = 0L
            }
        }
        Timber.d("clear: count reset to 0")
    }
}
