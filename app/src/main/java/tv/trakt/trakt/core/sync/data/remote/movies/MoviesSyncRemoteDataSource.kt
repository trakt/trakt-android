package tv.trakt.trakt.core.sync.data.remote.movies

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.WatchlistMovieDto
import java.time.Instant

internal interface MoviesSyncRemoteDataSource {
    suspend fun addToHistory(
        movieId: TraktId,
        watchedAt: Instant,
    )

    suspend fun getWatchlist(
        sort: String = "rank",
        page: Int? = null,
        limit: Int? = null,
        extended: String? = null,
    ): List<WatchlistMovieDto>
}
