package tv.trakt.trakt.app.core.search.data.local

import tv.trakt.trakt.app.core.movies.model.Movie
import tv.trakt.trakt.app.core.search.data.local.model.RecentMovieEntity
import tv.trakt.trakt.app.core.search.data.local.model.RecentShowEntity
import tv.trakt.trakt.app.core.shows.model.Show
import java.time.Instant

/**
 * Local data source for managing recent searches.
 * Provides methods to add and retrieve recently searched shows and movies.
 */
internal interface RecentSearchLocalDataSource {
    /**
     * Adds a show to the recent searches.
     *
     * @param show The [Show] to add.
     * @param addedAt The [Instant] the show was added. Defaults to the current time.
     */
    suspend fun addShow(
        show: Show,
        limit: Int = 10,
        addedAt: Instant = Instant.now(),
    )

    /**
     * Adds a movie to the recent searches.
     *
     * @param movie The [Movie] to add.
     * @param addedAt The [Instant] the movie was added. Defaults to the current time.
     */
    suspend fun addMovie(
        movie: Movie,
        limit: Int = 10,
        addedAt: Instant = Instant.now(),
    )

    /**
     * Retrieves a list of recently searched shows.
     *
     * @return A list of [RecentShowEntity] representing the recently searched shows.
     */
    suspend fun getShows(): List<RecentShowEntity>

    /**
     * Retrieves a list of recently searched movies.
     *
     * @return A list of [RecentMovieEntity] representing the recently searched movies.
     */
    suspend fun getMovies(): List<RecentMovieEntity>

    /**
     * Clears all recent searches.
     * This method is used to reset the recent searches data.
     */
    suspend fun clear()
}
