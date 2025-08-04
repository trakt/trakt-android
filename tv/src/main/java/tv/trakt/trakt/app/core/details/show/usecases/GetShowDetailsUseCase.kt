package tv.trakt.trakt.app.core.details.show.usecases

import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

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
