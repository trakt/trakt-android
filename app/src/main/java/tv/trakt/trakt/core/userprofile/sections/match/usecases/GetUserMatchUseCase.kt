package tv.trakt.trakt.core.userprofile.sections.match.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.common.model.fromDto

internal class GetUserMatchUseCase(
    private val userRemoteSource: UserRemoteSource,
    private val moviesRemoteSource: MoviesRemoteSource,
    private val showsRemoteSource: ShowsRemoteSource,
    private val movieLocalSource: MovieLocalSource,
    private val showLocalSource: ShowLocalSource,
) {
    suspend fun getMatch(
        userSlug: SlugId,
        mode: MediaMode,
    ): UserMatch {
        val type = when (mode) {
            MediaMode.Media -> null
            MediaMode.Movies -> "movie"
            MediaMode.Shows -> "show"
        }

        return userRemoteSource
            .getUserMatch(userSlug, type)
            .let(UserMatch::fromDto)
    }

    suspend fun getMovies(ids: List<TraktId>): ImmutableList<Movie> {
        return coroutineScope {
            ids.map { id -> async { getMovie(id) } }
                .awaitAll()
                .filterNotNull()
                .toImmutableList()
        }
    }

    suspend fun getShows(ids: List<TraktId>): ImmutableList<Show> {
        return coroutineScope {
            ids.map { id -> async { getShow(id) } }
                .awaitAll()
                .filterNotNull()
                .toImmutableList()
        }
    }

    private suspend fun getMovie(id: TraktId): Movie? {
        movieLocalSource.getMovie(id)?.let { return it }

        return try {
            Movie.fromDto(moviesRemoteSource.getDetails(id))
                .also { movieLocalSource.upsertMovies(listOf(it)) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
        }
    }

    private suspend fun getShow(id: TraktId): Show? {
        showLocalSource.getShow(id)?.let { return it }

        return try {
            Show.fromDto(showsRemoteSource.getShowDetails(id))
                .also { showLocalSource.upsertShows(listOf(it)) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            null
        }
    }
}
