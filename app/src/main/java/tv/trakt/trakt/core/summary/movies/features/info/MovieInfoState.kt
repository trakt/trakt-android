package tv.trakt.trakt.core.summary.movies.features.info

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.core.summary.movies.features.info.usecase.GetMovieCrewUseCase

@Immutable
internal data class MovieInfoState(
    val movie: Movie? = null,
    val movieStats: MovieStatsDto? = null,
    val movieStudios: ImmutableList<String>? = null,
    val movieCrew: GetMovieCrewUseCase.Result? = null,
    val loading: LoadingState = Idle,
    val error: Exception? = null,
)
