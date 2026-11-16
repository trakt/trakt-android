package tv.trakt.trakt.core.profile.sections.thismonth.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.UserStats

/**
 * @property remote Full stats from the API, loaded only for VIP accounts.
 * @property ratingsCount Local ratings count, used when [remote] is not available.
 * @property commentsCount Comments count, used when [remote] is not available.
 */
@Immutable
data class AllTimeStats(
    val remote: UserStats? = null,
    val ratingsCount: Int? = null,
    val commentsCount: Int? = null,
)
