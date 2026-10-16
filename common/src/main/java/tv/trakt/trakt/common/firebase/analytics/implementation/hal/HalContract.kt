package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.Analytics

/**
 * Event names and dim keys accepted by HAL, mirroring trakt-web's `AnalyticsEvent.ts`.
 * HAL refuses the whole batch on an unknown event name, so new names must be allowlisted
 * in trakt-workers `EVENT_CONTRACTS.ts` before a client emits them.
 */
internal object HalContract {
    const val EVENT_PAGE_VIEW = "page_view"
    const val EVENT_DISCOVER_MODE = "discover-mode"
    const val EVENT_SHARE_ARRIVAL = "share.arrival"
    const val EVENT_SEARCH = "action.search"
    const val EVENT_REACT = "action.react"
    const val EVENT_RATE = "media-action.rate"
    const val EVENT_ADD_COMMENT = "media-action.add-comment"
    const val EVENT_DELETE_COMMENT = "media-action.delete-comment"
    const val EVENT_MARK_AS_WATCHED = "media-action.mark-as-watched"
    const val EVENT_WATCHLIST = "media-action.watchlist"
    const val EVENT_CHECK_IN = "media-action.check-in"
    const val EVENT_LIST = "media-action.list"
    const val EVENT_LIST_CREATE = "list-action.create"
    const val EVENT_LIST_EDIT = "list-action.edit"
    const val EVENT_LIST_DELETE = "list-action.delete"
    const val EVENT_LIST_LIKE = "list-action.like"
    const val EVENT_SMART_LIST_CREATE = "smart-list-action.create"
    const val EVENT_SMART_LIST_DELETE = "smart-list-action.delete"
    const val EVENT_VIP_UPGRADE = "vip-action.vip-upgrade"
    const val EVENT_TODAY_OPEN = "today.open"
    const val EVENT_TODAY_STORY = "today.story"
    const val EVENT_TODAY_CLOSE = "today.close"
    const val EVENT_TODAY_ACTION = "today.action"

    const val DIM_ROUTE = "route"
    const val DIM_SOURCE = "source"
    const val DIM_MODE = "mode"
    const val DIM_TYPE = "type"
    const val DIM_OUTCOME = "outcome"
    const val DIM_ACTION = "action"
    const val DIM_RATING = "rating"
    const val DIM_PLAN = "plan"
    const val DIM_STATE = "state"
    const val DIM_REASON = "reason"
    const val DIM_FRAME = "frame"

    const val METRIC_RATING = "rating"
    const val METRIC_DURATION = "duration"
    const val METRIC_FRAMES = "frames"
    const val METRIC_GROUPS = "groups"
}

internal fun halMediaMode(mode: String): String? {
    return when (mode.lowercase()) {
        "media" -> "media"
        "show", "shows" -> "show"
        "movie", "movies" -> "movie"
        else -> null
    }
}

internal fun halSearchMode(mode: String): String? {
    return when (val normalized = mode.lowercase()) {
        "people", "lists" -> normalized
        else -> halMediaMode(normalized)
    }
}

internal fun halUserType(vip: String?): String {
    return when (vip?.lowercase()) {
        "true" -> "vip"
        "false" -> "standard"
        else -> "anonymous"
    }
}

internal fun halCommentType(content: Analytics.CommentContent): String {
    return when (content) {
        Analytics.CommentContent.Text -> "text"
        Analytics.CommentContent.Gif -> "gif"
        Analytics.CommentContent.TextAndGif -> "text-and-gif"
    }
}
