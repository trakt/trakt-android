package tv.trakt.trakt.core.reactions.media.data.remote

import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3MediaReactionsSummaryResponse
import tv.trakt.trakt.common.networking.api.v3.model.reactions.V3UserMediaReaction

internal interface MediaReactionsRemoteDataSource {
    suspend fun getSummary(target: MediaReactionsTarget): V3MediaReactionsSummaryResponse

    suspend fun getUserReactions(target: MediaReactionsTarget): List<V3UserMediaReaction>

    suspend fun postUserReaction(
        target: MediaReactionsTarget,
        reaction: String,
    ): List<V3UserMediaReaction>

    suspend fun deleteUserReactions(
        target: MediaReactionsTarget,
        reactionIds: List<Long>,
    )
}
