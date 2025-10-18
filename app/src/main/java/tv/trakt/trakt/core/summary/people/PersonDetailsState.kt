package tv.trakt.trakt.core.summary.people

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show

@Immutable
internal data class PersonDetailsState(
    val personDetails: Person? = null,
    val personBackdropUrl: String? = null,
    val personShowCredits: ImmutableList<Show>? = null,
    val personMovieCredits: ImmutableList<Movie>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)
