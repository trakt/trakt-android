package tv.trakt.trakt.common.firebase.analytics.implementation

import tv.trakt.trakt.common.firebase.analytics.Analytics

private const val EVENT_PAGE_VIEW = "page_view"
private const val EVENT_DISCOVER_MODE = "discover-mode"
private const val EVENT_SHARE_ARRIVAL = "share.arrival"
private const val EVENT_REACT = "action.react"
private const val EVENT_RATE = "media-action.rate"
private const val EVENT_ADD_COMMENT = "media-action.add-comment"
private const val EVENT_DELETE_COMMENT = "media-action.delete-comment"
private const val EVENT_MARK_AS_WATCHED = "media-action.mark-as-watched"
private const val EVENT_WATCHLIST = "media-action.watchlist"

private const val DIM_ROUTE = "route"
private const val DIM_SOURCE = "source"
private const val DIM_MODE = "mode"
private const val DIM_TYPE = "type"
private const val DIM_OUTCOME = "outcome"
private const val DIM_ACTION = "action"
private const val DIM_RATING = "rating"

private const val MEDIA_MODE_TOGGLE_SOURCE = "media-mode-toggle"
private const val USER_PROPERTY_VIP = "vip"

private val ROUTE_INVALID_CHARACTERS = Regex("[^a-z0-9_.-]")

internal fun halRoute(screenName: String): String {
    return "/" + screenName.lowercase().replace(ROUTE_INVALID_CHARACTERS, "_")
}

internal fun halMediaMode(mode: String): String? {
    return when (mode.lowercase()) {
        "media" -> "media"
        "show", "shows" -> "show"
        "movie", "movies" -> "movie"
        else -> null
    }
}

internal fun halUserType(vip: String?): String {
    return when (vip?.lowercase()) {
        "true" -> "vip"
        "false" -> "standard"
        else -> "anonymous"
    }
}

internal class HalAnalytics(
    private val events: HalEvents,
    private val setCrashlyticsUserId: (String) -> Unit,
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val comments: Analytics.Comments,
    override val progress: Analytics.Progress,
    override val trivia: Analytics.Trivia,
    override val playback: Analytics.Playback,
) : Analytics {
    override fun setUserId(userId: String?) {
        userId?.let(setCrashlyticsUserId)
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        if (key != USER_PROPERTY_VIP) return
        events.setUserType(halUserType(value))
    }

    override fun logScreenView(screenName: String) {
        events.track(EVENT_PAGE_VIEW, dims = mapOf(DIM_ROUTE to halRoute(screenName)))
    }

    override fun logUserLogin(source: String) = Unit

    override fun logUserLogout() = Unit

    override fun logMediaModeClick(mode: String) {
        val mediaMode = halMediaMode(mode) ?: return
        events.track(
            EVENT_DISCOVER_MODE,
            dims = mapOf(
                DIM_SOURCE to MEDIA_MODE_TOGGLE_SOURCE,
                DIM_MODE to mediaMode,
            ),
        )
    }

    override fun logMediaMode(mode: String) = Unit

    override fun logShareArrival(
        type: String,
        result: String,
    ) {
        events.track(
            EVENT_SHARE_ARRIVAL,
            dims = mapOf(
                DIM_TYPE to type.lowercase(),
                DIM_OUTCOME to result.lowercase(),
            ),
        )
    }
}

internal class HalAnalyticsReactions(
    private val events: HalEvents,
) : Analytics.Reactions {
    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) = react(ACTION_ADD, TYPE_COMMENT)

    override fun logReactionRemove(source: String) = react(ACTION_REMOVE, TYPE_COMMENT)

    override fun logMediaReactionAdd(
        reaction: String,
        mediaType: String,
    ) = react(ACTION_ADD, mediaType.lowercase())

    override fun logMediaReactionRemove(
        reaction: String,
        mediaType: String,
    ) = react(ACTION_REMOVE, mediaType.lowercase())

    private fun react(
        action: String,
        type: String,
    ) {
        events.track(EVENT_REACT, dims = mapOf(DIM_ACTION to action, DIM_TYPE to type))
    }

    private companion object {
        const val ACTION_ADD = "add"
        const val ACTION_REMOVE = "remove"
        const val TYPE_COMMENT = "comment"
    }
}

internal class HalAnalyticsRatings(
    private val events: HalEvents,
) : Analytics.Ratings {
    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) {
        events.track(
            EVENT_RATE,
            dims = mapOf(DIM_ACTION to "added", DIM_RATING to rating.toString()),
            metrics = mapOf(DIM_RATING to rating),
        )
    }

    override fun logRatingRemove(mediaType: String) {
        events.track(EVENT_RATE, dims = mapOf(DIM_ACTION to "removed"))
    }

    override fun logFavoriteAdd(mediaType: String) = Unit

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) = Unit
}

internal class HalAnalyticsComments(
    private val events: HalEvents,
) : Analytics.Comments {
    override fun logCommentAdd(mediaType: String) = addComment("post")

    override fun logReplyAdd() = addComment("reply")

    override fun logCommentRemove() = deleteComment()

    override fun logReplyRemove() = deleteComment()

    override fun logCommentTranslate(characters: Int) = Unit

    private fun addComment(action: String) {
        events.track(EVENT_ADD_COMMENT, dims = mapOf(DIM_ACTION to action, DIM_TYPE to "text"))
    }

    private fun deleteComment() {
        events.track(EVENT_DELETE_COMMENT)
    }
}

internal class HalAnalyticsProgress(
    private val events: HalEvents,
) : Analytics.Progress {
    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
        date: String?,
    ) = track(EVENT_MARK_AS_WATCHED, "add")

    override fun logRemoveWatchedMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_MARK_AS_WATCHED, "remove")

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_WATCHLIST, "add")

    override fun logRemoveWatchlistMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_WATCHLIST, "remove")

    private fun track(
        event: String,
        action: String,
    ) {
        events.track(event, dims = mapOf(DIM_ACTION to action))
    }
}

internal class HalAnalyticsTrivia : Analytics.Trivia {
    override fun logScreenView(source: String) = Unit
}

internal class HalAnalyticsPlayback : Analytics.Playback {
    override fun logPlaybackStart(mediaType: String) = Unit

    override fun logPlaybackStop(mediaType: String) = Unit
}
