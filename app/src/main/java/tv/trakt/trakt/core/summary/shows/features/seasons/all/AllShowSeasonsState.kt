package tv.trakt.trakt.core.summary.shows.features.seasons.all

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons

@Immutable
internal data class AllShowSeasonsState(
    val show: Show? = null,
    val user: User? = null,
    val backgroundUrl: String? = null,
    val items: ShowSeasons = ShowSeasons(),
    val loading: LoadingState = LoadingState.Idle,
    val loadingEpisode: LoadingState = LoadingState.Idle,
    val loadingSeason: LoadingState = LoadingState.Idle,
    val navigateEpisode: Pair<TraktId, Episode>? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
)
