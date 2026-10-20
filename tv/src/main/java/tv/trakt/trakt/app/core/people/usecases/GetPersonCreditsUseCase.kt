package tv.trakt.trakt.app.core.people.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.PeopleRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetPersonCreditsUseCase(
    private val peopleRemoteSource: PeopleRemoteSource,
    private val showLocalSource: ShowLocalSource,
    private val movieLocalSource: MovieLocalSource,
) {
    suspend fun getShowCredits(personId: TraktId): ImmutableList<Show> {
        val response = peopleRemoteSource.getPersonShowsCredits(personId)
        val shows = response.cast
            ?.map { Show.fromDto(it.show) }
            ?.sortedByDescending { it.rating.votes }

        shows?.let { showLocalSource.upsertShows(it) }

        return (shows ?: listOf()).toImmutableList()
    }

    suspend fun getMovieCredits(personId: TraktId): ImmutableList<Movie> {
        val response = peopleRemoteSource.getPersonMoviesCredits(personId)
        val movies = response.cast
            ?.map { Movie.fromDto(it.movie) }
            ?.sortedByDescending { it.rating.votes }

        movies?.let { movieLocalSource.upsertMovies(it) }

        return (movies ?: listOf()).toImmutableList()
    }
}
