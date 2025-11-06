package tv.trakt.trakt.analytics.implementation

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN
import com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW
import com.google.firebase.analytics.FirebaseAnalytics.Param
import tv.trakt.trakt.analytics.Analytics

// Android V3 events identifier
private const val EVENT_NAME_PREFIX = "av3_"

private const val EVENT_NAME_LIMIT = 40
private const val EVENT_NAME_ERROR = "Firebase event names must be less than 40 characters long"

private const val PARAMETER_MEDIA_TYPE = "media_type"
private const val PARAMETER_SOURCE = "source"
private const val PARAMETER_REACTION = "reaction"
private const val PARAMETER_RATING = "rating_value"

private fun eventName(name: String) = "$EVENT_NAME_PREFIX$name"

internal class FirebaseAnalytics(
    private val firebase: FirebaseAnalytics,
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val progress: Analytics.Progress,
) : Analytics {
    companion object Event {
        const val LOGOUT = "logout"
    }

    init {
        require(eventName(LOGOUT).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
    }

    override fun logScreenView(screenName: String) {
        firebase.logEvent(
            eventName(SCREEN_VIEW),
            bundleOf(Param.SCREEN_NAME to screenName),
        )
    }

    override fun logUserLogin() {
        firebase.logEvent(
            eventName(LOGIN),
            null,
        )
    }

    override fun logUserLogout() {
        firebase.logEvent(
            eventName(LOGOUT),
            null,
        )
    }
}

internal class FirebaseAnalyticsReactions(
    private val firebase: FirebaseAnalytics,
) : Analytics.Reactions {
    companion object Event {
        const val REACTIONS_ADD = "reactions_comment_add"
        const val REACTIONS_REMOVE = "reactions_comment_remove"
    }

    init {
        require(eventName(REACTIONS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(REACTIONS_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
    }

    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(REACTIONS_ADD),
            bundleOf(
                PARAMETER_REACTION to reaction.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }

    override fun logReactionRemove(source: String) {
        firebase.logEvent(
            eventName(REACTIONS_REMOVE),
            bundleOf(
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }
}

internal class FirebaseAnalyticsRatings(
    private val firebase: FirebaseAnalytics,
) : Analytics.Ratings {
    companion object Event {
        const val RATINGS_ADD = "ratings_add"
        const val FAVORITES_ADD = "favorites_add"
        const val FAVORITES_REMOVE = "favorites_remove"
    }

    init {
        require(eventName(RATINGS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(FAVORITES_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(FAVORITES_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
    }

    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) {
        firebase.logEvent(
            eventName(RATINGS_ADD),
            bundleOf(
                PARAMETER_RATING to rating,
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }

    override fun logFavoriteAdd(mediaType: String) {
        firebase.logEvent(
            eventName(FAVORITES_ADD),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(FAVORITES_REMOVE),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }
}

internal class FirebaseAnalyticsProgress(
    private val firebase: FirebaseAnalytics,
) : Analytics.Progress {
    companion object Event {
        const val PROGRESS_WATCHED_ADD = "progress_watched_add"
        const val PROGRESS_WATCHED_REMOVE = "progress_watched_remove"
        const val PROGRESS_WATCHLIST_ADD = "progress_watchlist_add"
        const val PROGRESS_WATCHLIST_REMOVE = "progress_watchlist_remove"
    }

    init {
        require(eventName(PROGRESS_WATCHED_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHED_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHLIST_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHLIST_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
    }

    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(PROGRESS_WATCHED_ADD),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }

    override fun logRemoveWatchedMedia(
        mediaType: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(PROGRESS_WATCHED_REMOVE),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(PROGRESS_WATCHLIST_ADD),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }

    override fun logRemoveWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(PROGRESS_WATCHLIST_REMOVE),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }
}
