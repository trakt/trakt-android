package tv.trakt.trakt.core.summary.shows.features.info.usecase

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ShowStatsDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowStatsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getStats(showId: TraktId): ShowStatsDto {
        return remoteSource.getStats(showId)
    }
}
