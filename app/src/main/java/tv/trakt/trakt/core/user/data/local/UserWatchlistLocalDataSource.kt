package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import java.time.Instant

internal interface UserWatchlistLocalDataSource {
    suspend fun setMovies(
        movies: List<WatchlistItem.MovieItem>,
        notify: Boolean = false,
    )

    suspend fun setShows(
        shows: List<WatchlistItem.ShowItem>,
        notify: Boolean = false,
    )

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun getMovies(): List<WatchlistItem.MovieItem>

    suspend fun getShows(): List<WatchlistItem.ShowItem>

    suspend fun getAll(): List<WatchlistItem>

    suspend fun removeMovies(
        ids: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun removeShows(
        ids: Set<TraktId>,
        notify: Boolean = false,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
