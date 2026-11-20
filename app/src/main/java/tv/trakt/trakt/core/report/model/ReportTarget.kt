package tv.trakt.trakt.core.report.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface ReportTarget {
    data class Comment(
        val id: Int,
    ) : ReportTarget

    data class User(
        val id: TraktId,
    ) : ReportTarget

    val type: String
        get() = when (this) {
            is Comment -> "comment"
            is User -> "user"
        }

    val reasons: ImmutableList<ReportReason>
        get() = when (this) {
            is Comment -> commentReasons
            is User -> userReasons
        }
}

private val commentReasons = persistentListOf(
    ReportReason.Abusive,
    ReportReason.Spam,
    ReportReason.Spoilers,
    ReportReason.Bigotry,
    ReportReason.Political,
    ReportReason.Duplicate,
    ReportReason.OffTopic,
    ReportReason.Other,
)

private val userReasons = persistentListOf(
    ReportReason.Spam,
    ReportReason.Adult,
    ReportReason.Language,
    ReportReason.Other,
)
