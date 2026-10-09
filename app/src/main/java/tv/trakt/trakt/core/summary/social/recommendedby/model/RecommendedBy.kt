package tv.trakt.trakt.core.summary.social.recommendedby.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.api.v3.model.V3RecommendedByResponse

@Immutable
internal data class RecommendedBy(
    val users: ImmutableList<User>,
    val otherCount: Int,
) {
    val totalCount: Int
        get() = users.size + otherCount

    val hasSharers: Boolean
        get() = totalCount > 0

    companion object {
        fun fromDto(dto: V3RecommendedByResponse): RecommendedBy {
            return RecommendedBy(
                users = dto.users.map(User::fromDto).toImmutableList(),
                otherCount = dto.otherCount,
            )
        }
    }
}
