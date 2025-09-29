package tv.trakt.trakt.core.search.data.local.popular

import tv.trakt.trakt.core.search.data.local.model.PopularMovieEntity
import tv.trakt.trakt.core.search.data.local.model.PopularShowEntity

internal interface PopularSearchLocalDataSource {
    suspend fun setShows(shows: List<PopularShowEntity>)

    suspend fun getShows(): List<PopularShowEntity>

    suspend fun setMovies(movies: List<PopularMovieEntity>)

    suspend fun getMovies(): List<PopularMovieEntity>

    suspend fun clear()
}
