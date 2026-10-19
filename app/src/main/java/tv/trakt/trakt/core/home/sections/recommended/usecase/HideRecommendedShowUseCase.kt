package tv.trakt.trakt.core.home.sections.recommended.usecase

import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.data.media.RecommendedMediaLocalDataSource
import tv.trakt.trakt.core.home.sections.recommended.data.shows.RecommendedShowsLocalDataSource

internal class HideRecommendedShowUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val localRecommendedSource: RecommendedShowsLocalDataSource,
    private val localRecommendedMediaSource: RecommendedMediaLocalDataSource,
    private val analytics: Analytics,
) {
    suspend fun hideShow(showId: TraktId) {
        remoteSource.hideRecommendation(showId)
        localRecommendedSource.removeShow(showId)
        localRecommendedMediaSource.removeItem(id = showId, type = MediaType.Show)
        analytics.progress.logHideRecommendation(MediaType.Show.value)
    }
}
