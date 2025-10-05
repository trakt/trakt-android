package tv.trakt.trakt.core.search.usecase.popular

import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.search.data.local.model.PopularMovieEntity
import tv.trakt.trakt.core.search.data.local.model.PopularShowEntity
import tv.trakt.trakt.core.search.data.local.model.create
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchLocalDataSource
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

private const val TRENDING_SEARCH_LIMIT = 36

internal class GetPopularSearchUseCase(
    private val remoteSource: SearchRemoteDataSource,
    private val localSource: PopularSearchLocalDataSource,
) {
    suspend fun getLocalShows(): List<PopularShowEntity> {
        val localShows = localSource.getShows()
        val timestamp = localShows.firstOrNull()?.createdAt?.toInstant()

        return when {
            isTimestampValid(timestamp) -> localShows
            else -> emptyList()
        }
    }

    suspend fun getLocalMovies(): List<PopularMovieEntity> {
        val localMovies = localSource.getMovies()
        val timestamp = localMovies.firstOrNull()?.createdAt?.toInstant()

        return when {
            isTimestampValid(timestamp) -> localMovies
            else -> emptyList()
        }
    }

    suspend fun getShows(): List<PopularShowEntity> {
        return remoteSource.getPopularShows(
            limit = TRENDING_SEARCH_LIMIT,
        ).asyncMap {
            PopularShowEntity.create(
                show = Show.fromDto(it.show!!),
                rank = it.count.toInt(),
                createdAt = nowUtcInstant(),
            )
        }.also {
            localSource.setShows(it)
        }
    }

    suspend fun getMovies(): List<PopularMovieEntity> {
        return remoteSource.getPopularMovies(
            limit = TRENDING_SEARCH_LIMIT,
        ).asyncMap {
            PopularMovieEntity.create(
                movie = Movie.fromDto(it.movie!!),
                rank = it.count.toInt(),
                createdAt = nowUtcInstant(),
            )
        }.also {
            localSource.setMovies(it)
        }
    }

    private fun isTimestampValid(timestamp: Instant?): Boolean {
        return timestamp?.plus(12, HOURS)?.isAfter(nowUtcInstant()) == true
    }
}
