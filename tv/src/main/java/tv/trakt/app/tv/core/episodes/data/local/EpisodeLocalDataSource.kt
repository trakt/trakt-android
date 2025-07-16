package tv.trakt.app.tv.core.episodes.data.local

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.episodes.model.Episode

internal interface EpisodeLocalDataSource {
    suspend fun getEpisode(episodeId: TraktId): Episode?

    suspend fun upsertEpisodes(episodes: List<Episode>)
}
