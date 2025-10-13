package tv.trakt.trakt.app.core.details.episode.usecases.streamings

import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetPlexUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val remoteShowSource: ShowsRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
) {
    suspend fun getPlexStatus(
        showId: TraktId,
        episodeId: TraktId,
    ): Result {
        var show = localShowSource.getShow(showId)

        // If we don't have the show or it doesn't have a Plex ID, fetch details from remote and update.
        if (show == null || show.ids.plex == null) {
            show = remoteShowSource.getShowDetails(showId)
                ?.let { Show.fromDto(it) }
                ?.also { localShowSource.upsertShows(listOf(it)) }
        }

        val result = remoteSyncSource.getEpisodesPlexCollection()
        return Result(
            isPlex = result.containsKey(episodeId) && show?.ids?.plex != null,
            plexSlug = show?.ids?.plex,
        )
    }

    data class Result(
        val isPlex: Boolean,
        val plexSlug: SlugId?,
    )
}
