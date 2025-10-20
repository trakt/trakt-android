package tv.trakt.trakt.core.reactions.data.remote

import tv.trakt.trakt.common.networking.UserReactionDto

internal interface ReactionsRemoteDataSource {
    suspend fun getUserReactions(): List<UserReactionDto>

    suspend fun postUserReaction(
        commentId: Int,
        reaction: String,
    )

    suspend fun deleteUserReactions(commentId: Int)
}
