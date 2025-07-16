package tv.trakt.app.tv.core.sync.data.remote.movies

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.WatchedMovieDto
import tv.trakt.app.tv.networking.openapi.WatchlistMovieDto
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
}
