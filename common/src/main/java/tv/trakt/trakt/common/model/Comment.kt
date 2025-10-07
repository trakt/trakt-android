package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.CommentDto
import java.time.ZonedDateTime

@Immutable
data class Comment(
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

    val commentNoSpoilers: String
        get() = comment.replace("[spoiler]", "", ignoreCase = true)
            .replace("[/spoiler]", "", ignoreCase = true)
            .trim()

    val userLiteRating: LiteRating?
        get() = userRating?.let { LiteRating.fromValue(it) }

    companion object {
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
