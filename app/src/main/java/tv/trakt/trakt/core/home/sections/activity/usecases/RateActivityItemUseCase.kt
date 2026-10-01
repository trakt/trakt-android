package tv.trakt.trakt.core.home.sections.activity.usecases

import android.content.Context
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker

internal class RateActivityItemUseCase(
    private val appContext: Context,
    private val sessionManager: SessionManager,
) {
    suspend fun rateItem(
        item: HomeActivityItem,
        rating: Int?,
        ratings: ImmutableMap<String, UserRating>?,
    ): ImmutableMap<String, UserRating>? {
        if (!sessionManager.isAuthenticated()) {
            return null
        }

        val current = ratings ?: emptyMap()
        if (current[item.key]?.rating == rating) {
            return null
        }

        val (mediaId, mediaType) = when (item) {
            is HomeActivityItem.MovieItem -> item.movie.ids.trakt to MediaType.Movie
            is HomeActivityItem.EpisodeItem -> item.episode.ids.trakt to MediaType.Episode
        }

        PostRatingWorker.scheduleOneTime(
            appContext = appContext,
            mediaId = mediaId,
            mediaType = mediaType,
            rating = rating ?: 0, // A rating of 0 indicates removal of rating
        )

        return when (rating) {
            null -> current - item.key
            else -> current + (
                item.key to UserRating(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    rating = rating,
                )
            )
        }.toImmutableMap()
    }
}
