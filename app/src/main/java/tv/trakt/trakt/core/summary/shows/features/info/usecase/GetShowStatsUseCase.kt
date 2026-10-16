package tv.trakt.trakt.core.summary.shows.features.info.usecase

import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ShowStatsDto

internal class GetShowStatsUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getStats(showId: TraktId): ShowStatsDto {
        return remoteSource.getStats(showId)
    }
}
