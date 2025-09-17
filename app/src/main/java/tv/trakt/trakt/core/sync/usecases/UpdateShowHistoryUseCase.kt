package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class UpdateShowHistoryUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
) {
    suspend fun dropShow(showId: TraktId) {
        remoteSource.dropShow(showId)
    }
}
