package tv.trakt.trakt.core.summary.shows.usecases

import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowDetailsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getLocalShow(showId: TraktId): Show? {
        return localSource.getShow(showId)
    }

    suspend fun getShow(showId: TraktId): Show? {
        return remoteSource.getShowDetails(showId)
            .let { Show.fromDto(it) }
            .also {
                localSource.upsertShows(listOf(it))
            }
    }
}
