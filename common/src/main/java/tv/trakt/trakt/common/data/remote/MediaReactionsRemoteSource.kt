package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.networking.api.v3.V3ReactionsApi
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class MediaReactionsRemoteSource(
    private val v3ReactionsApi: V3ReactionsApi,
    private val cacheMarker: CacheMarkerProvider,
) {
    suspend fun getSummary(target: MediaReactionsTarget): V3MediaReactionsSummaryResponse {
        return v3ReactionsApi.getMediaReactionsSummary(
            type = target.type,
            mediaId = target.id,
        )
    }

    suspend fun getUserReactions(target: MediaReactionsTarget): List<V3UserMediaReaction> {
        return v3ReactionsApi.getUserMediaReactions(
            type = target.type,
            mediaId = target.id,
        )
    }

    suspend fun postUserReactions(
        target: MediaReactionsTarget,
        reactions: List<String>,
    ): List<V3UserMediaReaction> {
        return v3ReactionsApi.putMediaReactions(
            type = target.type,
            mediaId = target.id,
            reactions = reactions,
        ).also {
            cacheMarker.invalidate()
        }
    }

    suspend fun deleteUserReactions(
        target: MediaReactionsTarget,
        reactionIds: List<Long>,
    ) {
        v3ReactionsApi.deleteMediaReactions(
            type = target.type,
            mediaId = target.id,
            reactionIds = reactionIds,
        )
        cacheMarker.invalidate()
    }
}
