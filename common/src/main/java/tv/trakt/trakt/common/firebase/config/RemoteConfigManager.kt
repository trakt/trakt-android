package tv.trakt.trakt.common.firebase.config

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import timber.log.Timber
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.R
import kotlin.time.Duration.Companion.minutes

private val MINIMUM_FETCH_INTERVAL = 5.minutes

class RemoteConfigManager {
    private val remoteConfig = Firebase.remoteConfig
    private val activations = MutableStateFlow(0L)

    fun setup() {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = when {
                    BuildConfig.DEBUG -> 0
                    else -> MINIMUM_FETCH_INTERVAL.inWholeSeconds
                }
            },
        )
    }

    fun refresh() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "Remote Config fetch failed")
                    return@addOnCompleteListener
                }
                Timber.d("Remote Config updated: %s", task.result)
                activations.update { it + 1 }
            }
    }

    /**
     * Emits [read] now and after every activation. While collected, realtime updates to [keys]
     * are activated immediately instead of waiting for the next [refresh].
     */
    fun <T> observe(
        keys: Set<String>,
        read: FirebaseRemoteConfig.() -> T,
    ): Flow<T> {
        return merge(activations, realtimeActivations(keys))
            .map { remoteConfig.read() }
            .distinctUntilChanged()
    }

    private fun realtimeActivations(keys: Set<String>): Flow<Long> {
        return callbackFlow {
            val registration = remoteConfig.addOnConfigUpdateListener(
                object : ConfigUpdateListener {
                    override fun onUpdate(configUpdate: ConfigUpdate) {
                        if (configUpdate.updatedKeys.none { it in keys }) {
                            return
                        }
                        remoteConfig.activate().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                activations.update { it + 1 }
                            }
                        }
                    }

                    override fun onError(error: FirebaseRemoteConfigException) {
                        Timber.w(error, "Remote Config realtime update failed")
                    }
                },
            )

            awaitClose { registration.remove() }
        }
    }
}
