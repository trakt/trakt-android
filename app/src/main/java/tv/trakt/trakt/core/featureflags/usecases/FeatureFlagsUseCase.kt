package tv.trakt.trakt.core.featureflags.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.featureflags.model.FeatureFlag
import tv.trakt.trakt.core.featureflags.model.FeatureFlagAudience
import tv.trakt.trakt.core.featureflags.model.FeatureFlagPreview

internal class FeatureFlagsUseCase(
    private val sessionManager: SessionManager,
    private val dataStore: DataStore<Preferences>,
) {
    private val remoteConfig = Firebase.remoteConfig

    fun observePreviews(): Flow<ImmutableList<FeatureFlagPreview>> {
        return combine(
            sessionManager.observeProfile(),
            dataStore.data,
        ) { user, prefs ->
            FeatureFlag.entries
                .map { it to audience(it) }
                .filter { (_, audience) -> canSeePreview(audience = audience, user = user) }
                .map { (flag, audience) ->
                    val locked = !canPreview(audience = audience, user = user)
                    FeatureFlagPreview(
                        flag = flag,
                        audience = audience,
                        enabled = !locked && previewEnabled(flag = flag, prefs = prefs),
                        locked = locked,
                    )
                }
                .toImmutableList()
        }.distinctUntilChanged()
    }

    fun observePreviewsVisible(): Flow<Boolean> {
        return combine(
            sessionManager.observeProfile(),
            observePreviews(),
        ) { user, previews ->
            user?.isDirector == true || previews.isNotEmpty()
        }.distinctUntilChanged()
    }

    fun observePreviewsUnseen(): Flow<Boolean> {
        return combine(
            observePreviews(),
            dataStore.data,
        ) { previews, prefs ->
            val seen = prefs[KEY_PREVIEWS_SEEN].orEmpty()
            previews.any { it.flag.id !in seen }
        }.distinctUntilChanged()
    }

    suspend fun markPreviewsSeen() {
        val previews = observePreviews().first().map { it.flag.id }
        dataStore.edit { prefs ->
            prefs[KEY_PREVIEWS_SEEN] = prefs[KEY_PREVIEWS_SEEN].orEmpty() + previews
        }
    }

    fun observeEnabled(flag: FeatureFlag): Flow<Boolean> {
        return combine(
            sessionManager.observeProfile(),
            dataStore.data,
        ) { user, prefs ->
            val audience = audience(flag)
            when {
                audience == FeatureFlagAudience.Public -> true
                canPreview(audience = audience, user = user) -> previewEnabled(flag = flag, prefs = prefs)
                else -> false
            }
        }.distinctUntilChanged()
    }

    suspend fun isEnabled(flag: FeatureFlag): Boolean {
        return observeEnabled(flag).first()
    }

    suspend fun setEnabled(
        flag: FeatureFlag,
        enabled: Boolean,
    ) {
        dataStore.edit { prefs ->
            prefs[enabledKey(flag)] = enabled
        }
    }

    private fun audience(flag: FeatureFlag): FeatureFlagAudience {
        return FeatureFlagAudience.fromRemoteValue(
            remoteConfig.getString(flag.remoteKey),
        )
    }

    private fun canSeePreview(
        audience: FeatureFlagAudience,
        user: User?,
    ): Boolean {
        if (user == null) return false

        return audience == FeatureFlagAudience.Vip || canPreview(audience = audience, user = user)
    }

    private fun canPreview(
        audience: FeatureFlagAudience,
        user: User?,
    ): Boolean {
        if (user == null) return false

        return when (audience) {
            FeatureFlagAudience.Off, FeatureFlagAudience.Public -> false
            FeatureFlagAudience.Director -> user.isDirector
            FeatureFlagAudience.Vip -> user.isDirector || user.isAnyVip
        }
    }

    private fun previewEnabled(
        flag: FeatureFlag,
        prefs: Preferences,
    ): Boolean {
        return prefs[enabledKey(flag)] == true
    }

    private fun enabledKey(flag: FeatureFlag): Preferences.Key<Boolean> {
        return booleanPreferencesKey("key_preview_feature_enabled_${flag.id}")
    }

    private companion object {
        val KEY_PREVIEWS_SEEN = stringSetPreferencesKey("key_preview_features_seen")
    }
}
