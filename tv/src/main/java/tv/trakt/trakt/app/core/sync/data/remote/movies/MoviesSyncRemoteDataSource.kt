package tv.trakt.trakt.app.core.sync.data.remote.movies

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.WatchedMovieDto
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import java.time.ZonedDateTime

internal interface MoviesSyncRemoteDataSource {
    suspend fun getWatchlist(
        sort: String = "rank",
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
    ): List<WatchlistMovieDto>

    suspend fun getWatched(extended: String? = null): List<WatchedMovieDto>

    suspend fun addToWatchlist(movieId: TraktId)

    suspend fun removeFromWatchlist(movieId: TraktId)

    suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: ZonedDateTime,
    )

    suspend fun removeFromHistory(movieId: TraktId)

    suspend fun getMoviesPlexCollection(): Map<TraktId, String>
}
