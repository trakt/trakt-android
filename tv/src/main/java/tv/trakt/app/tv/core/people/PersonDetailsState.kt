package tv.trakt.app.tv.core.people

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.shows.model.Show

@Immutable
internal data class PersonDetailsState(
    val isLoading: Boolean = false,
    val personDetails: Person? = null,
    val personBackdropUrl: String? = null,
    val personShowCredits: ImmutableList<Show>? = null,
    val personMovieCredits: ImmutableList<Movie>? = null,
    val error: Exception? = null,
)
