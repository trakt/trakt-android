package tv.trakt.trakt.tv.core.episodes.data.local

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.episodes.model.Episode

internal interface EpisodeLocalDataSource {
    suspend fun getEpisode(episodeId: TraktId): Episode?

    suspend fun upsertEpisodes(episodes: List<Episode>)
}
