package tv.trakt.trakt.core.settings.features.usage

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.billing.model.VipUsageGroup

@Immutable
internal data class UsageState(
    val user: User? = null,
    val groups: ImmutableList<VipUsageGroup>? = null,
    val loading: LoadingState = LoadingState.Idle,
)
