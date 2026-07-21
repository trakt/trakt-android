package tv.trakt.trakt.common.core.user.data.local

import tv.trakt.trakt.common.core.sync.model.ProgressItem
import tv.trakt.trakt.common.model.TraktId

interface UserProgressLocalDataSource {
    suspend fun setMovies(movies: List<ProgressItem.MovieItem>)

    suspend fun setShows(shows: List<ProgressItem.ShowItem>)

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun getMovies(): List<ProgressItem.MovieItem>

    suspend fun getShows(ids: Set<TraktId>? = null): List<ProgressItem.ShowItem>

    suspend fun removeMovies(ids: Set<TraktId>)

    suspend fun removeShows(ids: Set<TraktId>)

    fun clear()
}
