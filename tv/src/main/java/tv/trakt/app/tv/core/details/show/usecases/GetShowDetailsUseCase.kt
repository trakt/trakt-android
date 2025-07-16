package tv.trakt.app.tv.core.details.show.usecases

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.app.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.core.shows.model.fromDto

internal class GetShowDetailsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getShowDetails(showId: TraktId): Show? {
        val localShow = localSource.getShow(showId)
        if (localShow != null) {
            return localShow
        }

        return remoteSource.getShowDetails(showId)
            ?.let { Show.fromDto(it) }
            ?.also { localSource.upsertShows(listOf(it)) }
    }
}
