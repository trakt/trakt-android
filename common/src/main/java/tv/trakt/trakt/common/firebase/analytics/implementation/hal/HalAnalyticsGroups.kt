package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ACTION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_FRAME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_RATING
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_REASON
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SOURCE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_STATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_TYPE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_ADD_COMMENT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_CHECK_IN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_DELETE_COMMENT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_CREATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_DELETE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_EDIT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_LIKE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_MARK_AS_WATCHED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_RATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_REACT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SMART_LIST_CREATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SMART_LIST_DELETE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_ACTION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_CLOSE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_OPEN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_STORY
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_WATCHLIST
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_DURATION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_FRAMES
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_GROUPS
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_RATING

private const val ACTION_ADD = "add"
private const val ACTION_REMOVE = "remove"

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
        const val TYPE_COMMENT = "comment"
    }
}

internal class HalAnalyticsRatings(
    private val events: HalEvents,
) : Analytics.Ratings {
    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) = rate("added", rating)

    override fun logRatingChange(
        rating: Int,
        mediaType: String,
    ) = rate("changed", rating)

    override fun logRatingRemove(mediaType: String) {
        events.track(EVENT_RATE, dims = mapOf(DIM_ACTION to "removed"))
    }

    override fun logFavoriteAdd(mediaType: String) = Unit

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) = Unit

    private fun rate(
        action: String,
        rating: Int,
    ) {
        events.track(
            EVENT_RATE,
            dims = mapOf(DIM_ACTION to action, DIM_RATING to rating.toString()),
            metrics = mapOf(METRIC_RATING to rating.toLong()),
        )
    }
}

internal class HalAnalyticsComments(
    private val events: HalEvents,
) : Analytics.Comments {
    override fun logCommentAdd(
        mediaType: String,
        content: Analytics.CommentContent,
    ) = trackComment("post", content)

    override fun logReplyAdd(content: Analytics.CommentContent) = trackComment("reply", content)

    override fun logCommentEdit(content: Analytics.CommentContent) = trackComment("edit", content)

    override fun logCommentRemove() = deleteComment()

    override fun logReplyRemove() = deleteComment()

    override fun logCommentTranslate(characters: Int) = Unit

    private fun trackComment(
        action: String,
        content: Analytics.CommentContent,
    ) {
        events.track(
            EVENT_ADD_COMMENT,
            dims = mapOf(DIM_ACTION to action, DIM_TYPE to halCommentType(content)),
        )
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
    ) = track(EVENT_MARK_AS_WATCHED, ACTION_ADD)

    override fun logRemoveWatchedMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_MARK_AS_WATCHED, ACTION_REMOVE)

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_WATCHLIST, ACTION_ADD)

    override fun logRemoveWatchlistMedia(
        mediaType: String,
        source: String,
    ) = track(EVENT_WATCHLIST, ACTION_REMOVE)

    override fun logCheckInStart(mediaType: String) = checkIn(mediaType, "start")

    override fun logCheckInStop(mediaType: String) = checkIn(mediaType, "stop")

    private fun track(
        event: String,
        action: String,
    ) {
        events.track(event, dims = mapOf(DIM_ACTION to action))
    }

    private fun checkIn(
        mediaType: String,
        action: String,
    ) {
        events.track(EVENT_CHECK_IN, dims = mapOf(DIM_TYPE to mediaType.lowercase(), DIM_ACTION to action))
    }
}

internal class HalAnalyticsLists(
    private val events: HalEvents,
) : Analytics.Lists {
    override fun logListItemAdd() {
        events.track(EVENT_LIST, dims = mapOf(DIM_ACTION to ACTION_ADD))
    }

    override fun logListItemRemove() {
        events.track(EVENT_LIST, dims = mapOf(DIM_ACTION to ACTION_REMOVE))
    }

    override fun logListCreate() = events.track(EVENT_LIST_CREATE)

    override fun logListEdit() = events.track(EVENT_LIST_EDIT)

    override fun logListDelete() = events.track(EVENT_LIST_DELETE)

    override fun logListLike(liked: Boolean) {
        events.track(EVENT_LIST_LIKE, dims = mapOf(DIM_ACTION to if (liked) "like" else "unlike"))
    }

    override fun logSmartListCreate() = events.track(EVENT_SMART_LIST_CREATE)

    override fun logSmartListDelete() = events.track(EVENT_SMART_LIST_DELETE)
}

internal class HalAnalyticsToday(
    private val events: HalEvents,
) : Analytics.Today {
    override fun logOpen(
        source: String,
        hasUnseen: Boolean,
    ) {
        events.track(
            EVENT_TODAY_OPEN,
            dims = mapOf(
                DIM_SOURCE to source,
                DIM_STATE to if (hasUnseen) "unseen" else "seen",
            ),
        )
    }

    override fun logStory(type: String) {
        events.track(EVENT_TODAY_STORY, dims = mapOf(DIM_TYPE to type))
    }

    override fun logClose(
        reason: String,
        durationMs: Long,
        frames: Int,
        groups: Int,
    ) {
        events.track(
            EVENT_TODAY_CLOSE,
            dims = mapOf(DIM_REASON to reason),
            metrics = mapOf(
                METRIC_DURATION to durationMs,
                METRIC_FRAMES to frames.toLong(),
                METRIC_GROUPS to groups.toLong(),
            ),
        )
    }

    override fun logAction(
        action: String,
        frame: String,
    ) {
        events.track(EVENT_TODAY_ACTION, dims = mapOf(DIM_ACTION to action, DIM_FRAME to frame))
    }
}

internal class HalAnalyticsTrivia : Analytics.Trivia {
    override fun logScreenView(source: String) = Unit
}

internal class HalAnalyticsPlayback : Analytics.Playback {
    override fun logPlaybackStart(mediaType: String) = Unit

    override fun logPlaybackStop(mediaType: String) = Unit
}
