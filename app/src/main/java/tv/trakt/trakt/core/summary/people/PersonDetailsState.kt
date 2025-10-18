package tv.trakt.trakt.core.summary.people

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class PersonDetailsState(
    val personDetails: Person? = null,
    val personBackdropUrl: String? = null,
    val personShowCredits: ImmutableMap<String, ImmutableList<Show>>? = null,
    val personMovieCredits: ImmutableMap<String, ImmutableList<Movie>>? = null,
    val loadingDetails: LoadingState = LoadingState.IDLE,
    val loadingCredits: LoadingState = LoadingState.IDLE,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val error: Exception? = null,
)
