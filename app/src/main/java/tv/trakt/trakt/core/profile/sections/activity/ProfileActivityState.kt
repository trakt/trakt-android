package tv.trakt.trakt.core.profile.sections.activity

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem

@Immutable
internal data class ProfileActivityState(
    val ratingItems: ImmutableList<ProfileRatingItem>? = null,
    val commentItems: ImmutableList<ProfileCommentItem>? = null,
    val filter: ProfileActivityFilter = ProfileActivityFilter.Ratings,
    val reactions: ImmutableMap<Int, ReactionsSummary>? = null,
    val navigateShow: TraktId? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val navigateMovie: TraktId? = null,
    val collapsed: Boolean? = null,
    val loading: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)
