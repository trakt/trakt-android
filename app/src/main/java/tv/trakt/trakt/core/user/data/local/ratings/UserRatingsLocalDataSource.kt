package tv.trakt.trakt.core.user.data.local.ratings

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import java.time.Instant

internal interface UserRatingsLocalDataSource {
    suspend fun setShows(
        shows: List<UserRating>,
        notify: Boolean = false,
    )

    suspend fun setMovies(
        movies: List<UserRating>,
        notify: Boolean = false,
    )

    suspend fun setEpisodes(
        episodes: List<UserRating>,
        notify: Boolean = false,
    )

    suspend fun removeShows(
        ids: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun removeMovies(
        ids: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun removeEpisodes(
        ids: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun containsShow(id: TraktId): Boolean

    suspend fun containsMovie(id: TraktId): Boolean

    suspend fun containsEpisode(id: TraktId): Boolean

    suspend fun isShowsLoaded(): Boolean

    suspend fun isMoviesLoaded(): Boolean

    suspend fun isEpisodesLoaded(): Boolean

    suspend fun getAll(): List<UserRating>

    suspend fun getShows(): List<UserRating>

    suspend fun getMovies(): List<UserRating>

    suspend fun getEpisodes(): List<UserRating>

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
