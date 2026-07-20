package tv.trakt.trakt.common.core.user.data.local.watchlist.minimal

import tv.trakt.trakt.common.model.TraktId

interface UserWatchlistMinimalLocalDataSource {
    suspend fun setMovies(ids: Set<TraktId>)

    suspend fun setShows(ids: Set<TraktId>)

    suspend fun addMovies(movies: Set<TraktId>)

    suspend fun addShows(shows: Set<TraktId>)

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun getMovies(): Set<TraktId>

    suspend fun getShows(): Set<TraktId>

    suspend fun removeMovies(ids: Set<TraktId>)

    suspend fun removeShows(ids: Set<TraktId>)

    fun clear()
}
