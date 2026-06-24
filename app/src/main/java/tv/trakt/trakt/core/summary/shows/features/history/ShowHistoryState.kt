package tv.trakt.trakt.core.summary.shows.features.history

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import java.time.LocalDate

@Immutable
internal data class ShowHistoryState(
    val media: Media,
    val items: ImmutableMap<LocalDate, ImmutableList<HomeActivityItem.EpisodeItem>>? = null,
    val itemsRatings: ImmutableMap<String, UserRating>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val error: Exception? = null,
) {
    data class Media(
        val id: TraktId,
        val title: String,
        val background: String?,
        val watched: Int,
    )
}
