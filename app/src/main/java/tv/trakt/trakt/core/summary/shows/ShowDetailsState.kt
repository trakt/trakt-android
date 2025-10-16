package tv.trakt.trakt.core.summary.shows

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User

@Immutable
internal data class ShowDetailsState(
    val show: Show? = null,
    val showRatings: ExternalRating? = null,
    val showStudios: ImmutableList<String>? = null,
    val showProgress: ProgressState? = null,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingProgress: LoadingState = LoadingState.IDLE,
    val loadingLists: LoadingState = LoadingState.IDLE,
    val info: StringResource? = null,
    val error: Exception? = null,
    val user: User? = null,
) {
    data class ProgressState(
        val aired: Int,
        val plays: Int?,
        val inWatchlist: Boolean,
        val inLists: Boolean,
    ) {
        val inAnyList: Boolean = inWatchlist || inLists
    }
}
