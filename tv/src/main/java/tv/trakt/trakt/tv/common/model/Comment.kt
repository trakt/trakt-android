package tv.trakt.trakt.tv.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.networking.CommentDto
import tv.trakt.trakt.tv.helpers.LiteRating
import tv.trakt.trakt.tv.helpers.extensions.toZonedDateTime
import java.time.ZonedDateTime

@Immutable
internal data class Comment(
    val id: Int,
    val parentId: Int,
    val comment: String,
    val isSpoiler: Boolean,
    val isReview: Boolean,
    val replies: Int,
    val likes: Int,
    val userRating: Int?,
    val user: User,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    val hasSpoilers: Boolean
        get() = isSpoiler || comment.contains("[spoiler]", ignoreCase = true)

    val userLiteRating: LiteRating?
        get() = userRating?.let { LiteRating.fromValue(it) }

    internal companion object {
        fun fromDto(dto: CommentDto): Comment {
            return Comment(
                id = dto.id,
                parentId = dto.parentId,
                comment = dto.comment,
                isSpoiler = dto.spoiler,
                isReview = dto.review,
                replies = dto.replies,
                likes = dto.likes,
                userRating = dto.userRating,
                user = User.fromDto(dto.user),
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
            )
        }
    }
}
