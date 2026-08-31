package tv.trakt.trakt.core.home.sections.recommended.usecase

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class HideRecommendedShowUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val localRecommendedSource: RecommendedShowsLocalDataSource,
) {
    suspend fun hideShow(showId: TraktId) {
        remoteSource.hideRecommendation(showId)
        localRecommendedSource.removeShow(showId)
    }
}
