package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.model.ProgressItem
import java.time.Instant

internal interface UserProgressLocalDataSource {
    suspend fun setMovies(
        movies: List<ProgressItem.MovieItem>,
        notify: Boolean = false,
    )

    suspend fun setShows(
        shows: List<ProgressItem.ShowItem>,
        notify: Boolean = false,
    )

    suspend fun addMovies(
        movies: List<ProgressItem.MovieItem>,
        notify: Boolean = false,
    )

    suspend fun addShows(
        shows: List<ProgressItem.ShowItem>,
        notify: Boolean = false,
    )

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun getMovies(): List<ProgressItem.MovieItem>

    suspend fun getShows(): List<ProgressItem.ShowItem>

    suspend fun getAll(): List<ProgressItem>

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
