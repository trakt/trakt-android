package tv.trakt.trakt.core.search.usecase.popular

import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.search.data.local.model.PopularListEntity
import tv.trakt.trakt.core.search.data.local.model.PopularMovieEntity
import tv.trakt.trakt.core.search.data.local.model.PopularShowEntity
import tv.trakt.trakt.core.search.data.local.model.create
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchLocalDataSource
import java.time.temporal.ChronoUnit.HOURS

private const val POPULAR_SEARCH_LIMIT = 36
private const val POPULAR_LISTS_SEARCH_LIMIT = 50
private const val POPULAR_VALID_PERIOD_HOURS = 12L

internal class GetPopularSearchUseCase(
    private val remoteSource: SearchRemoteDataSource,
    private val localSource: PopularSearchLocalDataSource,
) {
    suspend fun isLocalShowsValid(): Boolean {
        val local = localSource.getShows()
        val timestamp = local.firstOrNull()?.createdAt?.toInstant()
        return timestamp?.plus(POPULAR_VALID_PERIOD_HOURS, HOURS)?.isAfter(nowUtcInstant()) == true
    }

    suspend fun isLocalMoviesValid(): Boolean {
        val local = localSource.getMovies()
        val timestamp = local.firstOrNull()?.createdAt?.toInstant()
        return timestamp?.plus(POPULAR_VALID_PERIOD_HOURS, HOURS)?.isAfter(nowUtcInstant()) == true
    }

    suspend fun isLocalListsValid(): Boolean {
        val local = localSource.getLists()
        val timestamp = local.firstOrNull()?.createdAt?.toInstant()
        return timestamp?.plus(POPULAR_VALID_PERIOD_HOURS, HOURS)?.isAfter(nowUtcInstant()) == true
    }

    suspend fun getLocalShows(): List<PopularShowEntity> {
        return localSource.getShows()
    }

    suspend fun getLocalMovies(): List<PopularMovieEntity> {
        return localSource.getMovies()
    }

    suspend fun getLocalLists(): List<PopularListEntity> {
        return localSource.getLists()
    }

    suspend fun getShows(): List<PopularShowEntity> {
        return remoteSource.getPopularShows(
            limit = POPULAR_SEARCH_LIMIT,
        ).asyncMap {
            PopularShowEntity.create(
                show = Show.fromDto(it.show!!),
                rank = it.count,
                createdAt = nowUtcInstant(),
            )
        }.also {
            localSource.setShows(it)
        }
    }

    suspend fun getMovies(): List<PopularMovieEntity> {
        return remoteSource.getPopularMovies(
            limit = POPULAR_SEARCH_LIMIT,
        ).asyncMap {
            PopularMovieEntity.create(
                movie = Movie.fromDto(it.movie!!),
                rank = it.count,
                createdAt = nowUtcInstant(),
            )
        }.also {
            localSource.setMovies(it)
        }
    }

    suspend fun getLists(): List<PopularListEntity> {
        return remoteSource.getPopularLists(
            limit = POPULAR_LISTS_SEARCH_LIMIT,
        ).asyncMap {
            PopularListEntity.create(
                list = CustomList.fromDto(it.list!!),
                createdAt = nowUtcInstant(),
            )
        }.also {
            localSource.setLists(it)
        }
    }
}
