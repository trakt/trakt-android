package tv.trakt.trakt.common.model

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import tv.trakt.trakt.common.helpers.extensions.isGoogleTranslateInstalled
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.networking.CommentDto
import java.time.ZonedDateTime
import java.util.Locale

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
    val language: Locale?,
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

    val user5Rating: String?
        get() = when {
            userRating == null -> null
            else -> "${userRating / 2F}".replace(".0", "")
        }

    @Composable
    fun rememberTranslatable(): Boolean {
        val context = LocalContext.current
        val configuration = LocalConfiguration.current

        val isGoogleTranslateInstalled = remember {
            context.isGoogleTranslateInstalled()
        }
        val appLocale = remember(configuration) {
            AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        }

        return remember(appLocale) {
            language?.language != null &&
                language.language != appLocale.language &&
                isGoogleTranslateInstalled
        }
    }

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
                language = dto.language?.let {
                    runCatching { Locale.forLanguageTag(it) }.getOrNull()
                },
                createdAt = dto.createdAt.toZonedDateTime(),
                updatedAt = dto.updatedAt.toZonedDateTime(),
            )
        }
    }
}
