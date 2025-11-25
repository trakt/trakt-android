package tv.trakt.trakt.analytics.implementation

import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.core.main.model.MediaMode

internal class DebugAnalytics(
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val comments: Analytics.Comments,
    override val progress: Analytics.Progress,
) : Analytics {
    override fun logScreenView(screenName: String) {
        Timber.d("logScreenView: screenName=$screenName")
    }

    override fun logUserLogin() {
        Timber.d("logUserLogin")
    }

    override fun logUserLogout() {
        Timber.d("logUserLogout")
    }

    override fun logMediaMode(mode: MediaMode) {
        Timber.d("logMediaMode: mode=${mode.name.lowercase()}")
    }

    override fun logMediaModeClick(mode: MediaMode) {
        Timber.d("logMediaModeClick: mode=${mode.name.lowercase()}")
    }
}

internal class DebugAnalyticsReactions : Analytics.Reactions {
    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) {
        Timber.d("logReactionAdd: reaction=${reaction.lowercase()}, source=${source.lowercase()}")
    }

    override fun logReactionRemove(source: String) {
        Timber.d("logReactionRemove: source=${source.lowercase()}")
    }
}

internal class DebugAnalyticsRatings : Analytics.Ratings {
    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) {
        Timber.d("logRatingAdd: rating=$rating, mediaType=${mediaType.lowercase()}")
    }

    override fun logFavoriteAdd(mediaType: String) {
        Timber.d("logFavoriteAdd: mediaType=${mediaType.lowercase()}")
    }

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logFavoriteRemove: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }
}

internal class DebugAnalyticsComments : Analytics.Comments {
    override fun logCommentAdd(mediaType: String) {
        Timber.d("logCommentAdd: mediaType=${mediaType.lowercase()}")
    }

    override fun logCommentRemove() {
        Timber.d("logCommentRemove")
    }

    override fun logReplyAdd() {
        Timber.d("logReplyAdd")
    }

    override fun logReplyRemove() {
        Timber.d("logReplyRemove")
    }
}

internal class DebugAnalyticsProgress : Analytics.Progress {
    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
        date: String?,
    ) {
        val dateParam = date ?: "now"
        Timber.d(
            "logAddWatchedMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}, date=$dateParam",
        )
    }

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logAddWatchlistMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }

    override fun logRemoveWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logRemoveWatchlistMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }

    override fun logRemoveWatchedMedia(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logRemoveWatchedMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }
}
