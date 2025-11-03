package tv.trakt.trakt.analytics.implementation

import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics

internal class DebugAnalytics(
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
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
}

internal class DebugAnalyticsProgress : Analytics.Progress {
    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logAddWatchedMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        Timber.d("logAddWatchlistMedia: mediaType=${mediaType.lowercase()}, source=${source.lowercase()}")
    }
}
