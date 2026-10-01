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
import kotlin.math.roundToInt

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
    val userStats: CommentUserStats,
    val user: User,
    val language: Locale?,
    val gif: CommentGif?,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
) {
    val hasSpoilers: Boolean
        get() = isSpoiler || comment.contains("[spoiler]", ignoreCase = true)

    val commentNoSpoilers: String
        get() = comment.replace("[spoiler]", "", ignoreCase = true)
            .replace("[/spoiler]", "", ignoreCase = true)
            .trim()

    val user5Rating: String?
        get() = when {
            userRating == null -> null
            else -> "${userRating / 2F}".replace(".0", "")
        }

    /**
     * @param onDeviceAvailable true when the caller can translate on device, so the
     * Google Translate app is not required.
     */
    @Composable
    fun rememberTranslatable(onDeviceAvailable: Boolean = false): Boolean {
        if (comment.isBlank()) return false

        val context = LocalContext.current
        val configuration = LocalConfiguration.current

        val isGoogleTranslateInstalled = remember {
            context.isGoogleTranslateInstalled()
        }
        val appLocale = remember(configuration) {
            AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        }

        return remember(appLocale, onDeviceAvailable) {
            language?.language != null &&
                language.language != appLocale.language &&
                (onDeviceAvailable || isGoogleTranslateInstalled)
        }
    }

    companion object {
        fun fromDto(dto: CommentDto): Comment {
            return Comment(
                id = dto.id,
                parentId = dto.parentId,
                comment = dto.comment,
                gif = dto.gif?.let {
                    CommentGif(
                        slug = it.slug ?: "",
                        url = it.url,
                        size = it.width to it.height,
                    )
                },
                isSpoiler = dto.spoiler,
                isReview = dto.review,
                replies = dto.replies,
                likes = dto.likes,
                userRating = dto.userRating,
                userStats = CommentUserStats(
                    playCount = dto.userStats.playCount,
                    completedCount = dto.userStats.completedCount,
                    rating = dto.userStats.rating,
                ),
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

@Immutable
data class CommentUserStats(
    val playCount: Int,
    val completedCount: Int,
    val rating: Int?,
) {
    fun progress(totalEpisodes: Int?): CommentUserProgress? {
        if (totalEpisodes == null || totalEpisodes <= 0) return null

        val completed = completedCount.coerceAtMost(totalEpisodes)
        if (completed <= 0) return null

        val percent = (completed * 100F / totalEpisodes).roundToInt()
        return CommentUserProgress(
            completed = completed,
            total = totalEpisodes,
            percent = "$percent%",
        )
    }
}

@Immutable
data class CommentUserProgress(
    val completed: Int,
    val total: Int,
    val percent: String,
)
