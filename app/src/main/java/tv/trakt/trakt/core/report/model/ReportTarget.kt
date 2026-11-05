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

    data class Movie(
        val id: TraktId,
    ) : ReportTarget

    data class Show(
        val id: TraktId,
    ) : ReportTarget

    data class Episode(
        val showId: TraktId,
        val season: Int,
        val episode: Int,
    ) : ReportTarget

    data class Person(
        val id: TraktId,
    ) : ReportTarget

    data class User(
        val id: TraktId,
    ) : ReportTarget

    val type: String
        get() = when (this) {
            is Comment -> "comment"
            is Movie -> "movie"
            is Show -> "show"
            is Episode -> "episode"
            is Person -> "person"
            is User -> "user"
        }

    val reasons: ImmutableList<ReportReason>
        get() = when (this) {
            is Comment -> commentReasons
            is Movie, is Show, is Episode -> mediaReasons
            is Person -> personReasons
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

private val mediaReasons = persistentListOf(
    ReportReason.Duplicate,
    ReportReason.Remove,
    ReportReason.DataRefresh,
    ReportReason.Metadata,
    ReportReason.Adult,
    ReportReason.Runtime,
    ReportReason.Language,
    ReportReason.Spam,
    ReportReason.Tmdb,
    ReportReason.Other,
)

private val personReasons = persistentListOf(
    ReportReason.Duplicate,
    ReportReason.Remove,
    ReportReason.DataRefresh,
    ReportReason.Metadata,
    ReportReason.Adult,
    ReportReason.Language,
    ReportReason.Spam,
    ReportReason.Tmdb,
    ReportReason.Other,
)

private val userReasons = persistentListOf(
    ReportReason.Spam,
    ReportReason.Adult,
    ReportReason.Language,
    ReportReason.Other,
)
