package tv.trakt.trakt.core.summary.sentiment.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.recommended.RecommendedSource

internal class GetMatchReasonsUseCase(
    private val remoteSource: MediaRemoteSource,
) {
    suspend fun getReasons(
        mediaId: TraktId,
        mediaType: MediaType,
    ): ImmutableList<RecommendedSource> {
        return remoteSource
            .getRecommendationReasons(
                type = mediaType,
                mediaId = mediaId,
            )
            .sources
            .orEmpty()
            .map(RecommendedSource::fromDto)
            .filter { it.movie != null || it.show != null }
            .toImmutableList()
    }
}
