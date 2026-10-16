package tv.trakt.trakt.core.summary.shows.usecases

import tv.trakt.trakt.common.core.media.shows.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetShowDetailsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getLocalShow(showId: TraktId): Show? {
        return localSource.getShow(showId)
    }

    suspend fun getShow(showId: TraktId): Show {
        return remoteSource.getShowDetails(showId)
            .let { Show.fromDto(it) }
            .also {
                localSource.upsertShows(listOf(it))
            }
    }

    suspend fun getShow(showSlug: SlugId): Show {
        return remoteSource.getShowDetails(showSlug)
            .let { Show.fromDto(it) }
            .also {
                localSource.upsertShows(listOf(it))
            }
    }
}
