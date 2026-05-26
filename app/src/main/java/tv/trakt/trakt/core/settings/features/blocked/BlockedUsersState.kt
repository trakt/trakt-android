package tv.trakt.trakt.core.settings.features.blocked

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.User

@Immutable
internal data class BlockedUsersState(
    val users: ImmutableList<User> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val info: StringResource? = null,
)
