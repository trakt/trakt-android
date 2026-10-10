package tv.trakt.trakt.common.core.media.episodes.local

import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId

interface EpisodeLocalDataSource {
    suspend fun getEpisode(episodeId: TraktId): Episode?

    suspend fun upsertEpisodes(episodes: List<Episode>)
}
