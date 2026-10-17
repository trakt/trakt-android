package tv.trakt.trakt.common.data.remote

import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class MediaReactionsRemoteSource(
    private val v3Api: V3Api,
    private val cacheMarker: CacheMarkerProvider,
) {
    suspend fun getSummary(target: MediaReactionsTarget): V3MediaReactionsSummaryResponse {
        return v3Api.getMediaReactionsSummary(
            type = target.type,
            mediaId = target.id,
        )
    }

    suspend fun getUserReactions(target: MediaReactionsTarget): List<V3UserMediaReaction> {
        return v3Api.getUserMediaReactions(
            type = target.type,
            mediaId = target.id,
        )
    }

    suspend fun postUserReactions(
        target: MediaReactionsTarget,
        reactions: List<String>,
    ): List<V3UserMediaReaction> {
        return v3Api.putMediaReactions(
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
        v3Api.deleteMediaReactions(
            type = target.type,
            mediaId = target.id,
            reactionIds = reactionIds,
        )
        cacheMarker.invalidate()
    }
}
