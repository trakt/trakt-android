package tv.trakt.trakt.core.home.sections.activity.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class AllActivityState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val usersFilter: UsersFilter = UsersFilter(),
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
) {
    data class UsersFilter(
        val users: ImmutableSet<User> = emptySet<User>().toImmutableSet(),
        val selectedUser: User? = null,
    )
}
