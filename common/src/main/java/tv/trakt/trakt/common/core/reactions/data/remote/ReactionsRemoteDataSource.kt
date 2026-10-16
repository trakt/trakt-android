package tv.trakt.trakt.common.core.reactions.data.remote

import tv.trakt.trakt.common.networking.UserReactionDto

interface ReactionsRemoteDataSource {
    suspend fun getUserReactions(): List<UserReactionDto>

    suspend fun postUserReaction(
        commentId: Int,
        reaction: String,
    )

    suspend fun deleteUserReactions(commentId: Int)
}
