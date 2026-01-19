package tv.trakt.trakt.core.home.sections.activity.features.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class HomeHistoryState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val itemsRatings: ImmutableMap<String, UserRating>? = null,
    val collapsed: Boolean? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val user: User? = null,
    val error: Exception? = null,
)
