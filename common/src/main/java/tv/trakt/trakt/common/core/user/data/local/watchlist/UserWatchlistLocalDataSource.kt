package tv.trakt.trakt.common.core.user.data.local.watchlist

import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.model.TraktId

interface UserWatchlistLocalDataSource {
    suspend fun setMovies(movies: List<WatchlistItem.MovieItem>)

    suspend fun setShows(shows: List<WatchlistItem.ShowItem>)

    suspend fun addMovies(movies: List<WatchlistItem.MovieItem>)

    suspend fun addShows(shows: List<WatchlistItem.ShowItem>)

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun getMovies(): List<WatchlistItem.MovieItem>

    suspend fun getShows(): List<WatchlistItem.ShowItem>

    suspend fun getAll(): List<WatchlistItem>

    suspend fun removeMovies(ids: Set<TraktId>)

    suspend fun removeShows(ids: Set<TraktId>)

    fun clear()
}
