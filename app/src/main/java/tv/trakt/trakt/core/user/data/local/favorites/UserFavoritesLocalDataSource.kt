package tv.trakt.trakt.core.user.data.local.favorites

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.model.FavoriteItem
import java.time.Instant

internal interface UserFavoritesLocalDataSource {
    suspend fun setMovies(
        movies: List<FavoriteItem.MovieItem>,
        notify: Boolean = false,
    )

    suspend fun setShows(
        shows: List<FavoriteItem.ShowItem>,
        notify: Boolean = false,
    )

    suspend fun addMovies(
        movies: List<FavoriteItem.MovieItem>,
        notify: Boolean = false,
    )

    suspend fun addShows(
        shows: List<FavoriteItem.ShowItem>,
        notify: Boolean = false,
    )

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun getMovies(): List<FavoriteItem.MovieItem>

    suspend fun getShows(): List<FavoriteItem.ShowItem>

    suspend fun getAll(): List<FavoriteItem>

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
