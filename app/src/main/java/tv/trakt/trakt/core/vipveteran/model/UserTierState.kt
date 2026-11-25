package tv.trakt.trakt.core.vipveteran.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserTier
import tv.trakt.trakt.common.model.VipVeteran

@Immutable
internal data class UserTierState(
    val enabled: Boolean = false,
    val ownId: TraktId? = null,
    val ownVeteran: VipVeteran? = null,
) {
    fun tierOf(user: User): UserTier? {
        if (!enabled) return null

        val veteran = when (user.ids.trakt) {
            ownId -> ownVeteran ?: user.veteran
            else -> user.veteran
        }
        return UserTier.from(
            isVip = user.isAnyVip,
            veteran = veteran,
        )
    }
}

internal val LocalUserTierState = compositionLocalOf { UserTierState() }
