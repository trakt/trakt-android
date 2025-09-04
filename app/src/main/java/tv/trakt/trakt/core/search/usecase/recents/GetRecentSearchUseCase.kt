package tv.trakt.trakt.core.search.usecase.recents

import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.core.search.data.local.model.RecentMovieEntity
import tv.trakt.trakt.core.search.data.local.model.RecentPersonEntity
import tv.trakt.trakt.core.search.data.local.model.RecentShowEntity

internal class GetRecentSearchUseCase(
    private val recentsLocalSource: RecentSearchLocalDataSource,
) {
    suspend fun getRecentShows(): List<RecentShowEntity> {
        return recentsLocalSource
            .getShows()
    }

    suspend fun getRecentMovies(): List<RecentMovieEntity> {
        return recentsLocalSource
            .getMovies()
    }

    suspend fun getRecentPeople(): List<RecentPersonEntity> {
        return recentsLocalSource
            .getPeople()
    }
}
