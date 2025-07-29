package tv.trakt.trakt.tv.core.people.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.movies.model.fromDto
import tv.trakt.trakt.tv.core.people.data.remote.PeopleRemoteDataSource
import tv.trakt.trakt.tv.core.shows.data.local.ShowLocalDataSource

internal class GetPersonCreditsUseCase(
    private val peopleRemoteSource: PeopleRemoteDataSource,
    private val showLocalSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
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

        movies?.let { movieLocalDataSource.upsertMovies(it) }

        return (movies ?: listOf()).toImmutableList()
    }
}
