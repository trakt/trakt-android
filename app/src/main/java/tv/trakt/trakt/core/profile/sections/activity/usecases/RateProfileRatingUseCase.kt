package tv.trakt.trakt.core.profile.sections.activity.usecases

import android.content.Context
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker

internal class RateProfileRatingUseCase(
    private val appContext: Context,
    private val sessionManager: SessionManager,
) {
    suspend fun rateItem(
        item: ProfileRatingItem,
        rating: Int?,
        origin: ChangeOrigin,
    ): Boolean {
        if (!sessionManager.isAuthenticated()) {
            return false
        }
        if (item.rating == rating) {
            return false
        }

        val userRating = item.userRating
        PostRatingWorker.scheduleOneTime(
            appContext = appContext,
            mediaId = userRating.mediaId,
            mediaType = userRating.mediaType,
            rating = rating ?: 0, // A rating of 0 indicates removal of rating
            origin = origin,
        )

        return true
    }
}

internal fun ImmutableList<ProfileRatingItem>.withItemRating(
    key: String,
    rating: Int?,
): ImmutableList<ProfileRatingItem> {
    return mapNotNull { item ->
        when {
            item.key != key -> item
            rating == null -> null
            else -> item.withRating(rating)
        }
    }.toImmutableList()
}
