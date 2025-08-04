package tv.trakt.trakt.app.core.episodes.data.local

import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.common.model.TraktId

internal interface EpisodeLocalDataSource {
    suspend fun getEpisode(episodeId: TraktId): Episode?

    suspend fun upsertEpisodes(episodes: List<Episode>)
}
