package tv.trakt.trakt.common.firebase.analytics.implementation.hal

import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ACTION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_ERROR
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_FLOW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_FRAME
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_PLAN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_REASON
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SLUG
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SORT_BY
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SORT_HOW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_SOURCE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_STATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.DIM_TYPE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_ADD_COMMENT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_CHECK_IN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_DELETE_COMMENT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_DROP
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_EXTRAS
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_FAVORITE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_GIF_PICKER
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_HIDE_RECOMMENDATION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_CREATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_DELETE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_EDIT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_LIKE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_LIST_SORT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_MARK_AS_WATCHED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_RATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_REACT
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SMART_LIST_CREATE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SMART_LIST_DELETE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_SPOILER_REVEAL
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_STREAM_ON
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_ACTION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_CLOSE
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_OPEN
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TODAY_STORY
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_TRAILER
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_CHECKOUT_CANCELED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_CHECKOUT_FAILED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_CONFIRMED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_CONFIRM_FAILED
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_UPSELL_IMPRESSION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_VIP_VIEW
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.EVENT_WATCHLIST
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_DURATION
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_FRAMES
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_GROUPS
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalContract.METRIC_RATING

private const val ACTION_ADD = "add"
private const val ACTION_REMOVE = "remove"
private const val COMMENT = "comment"

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

    override fun logFavoriteAdd(mediaType: String) {
        events.track(EVENT_FAVORITE, dims = mapOf(DIM_ACTION to ACTION_ADD))
    }

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) {
        events.track(EVENT_FAVORITE, dims = mapOf(DIM_ACTION to ACTION_REMOVE))
    }

    private fun rate(
        action: String,
        rating: Int,
    ) {
        events.track(
            EVENT_RATE,
            dims = mapOf(DIM_ACTION to action),
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

    override fun logSpoilerReveal() {
        events.track(EVENT_SPOILER_REVEAL, dims = mapOf(DIM_TYPE to COMMENT))
    }

    override fun logGifPicker(
        action: String,
        source: String,
    ) {
        events.track(
            EVENT_GIF_PICKER,
            dims = mapOf(DIM_ACTION to action, DIM_SOURCE to source),
        )
    }

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

    override fun logDrop(mediaType: String) {
        events.track(EVENT_DROP, dims = mapOf(DIM_TYPE to mediaType.lowercase()))
    }

    override fun logHideRecommendation(mediaType: String) {
        events.track(EVENT_HIDE_RECOMMENDATION, dims = mapOf(DIM_TYPE to mediaType.lowercase()))
    }

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

    override fun logListSort(
        sortBy: String,
        sortHow: String,
    ) {
        events.track(EVENT_LIST_SORT, dims = mapOf(DIM_SORT_BY to sortBy, DIM_SORT_HOW to sortHow))
    }
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

internal class HalAnalyticsPlayback(
    private val events: HalEvents,
) : Analytics.Playback {
    override fun logPlaybackStart(mediaType: String) = Unit

    override fun logPlaybackStop(mediaType: String) = Unit

    override fun logTrailer(slug: String) {
        events.track(EVENT_TRAILER, dims = mapOf(DIM_SLUG to slug))
    }

    override fun logExtras(
        slug: String,
        type: String,
    ) {
        events.track(EVENT_EXTRAS, dims = mapOf(DIM_SLUG to slug, DIM_TYPE to type.lowercase()))
    }

    override fun logStreamOn(source: String) {
        events.track(EVENT_STREAM_ON, dims = mapOf(DIM_SOURCE to source))
    }
}

internal class HalAnalyticsVip(
    private val events: HalEvents,
) : Analytics.Vip {
    override fun logUpsellImpression(source: String) {
        events.track(EVENT_VIP_UPSELL_IMPRESSION, dims = mapOf(DIM_SOURCE to source))
    }

    override fun logView(
        source: String,
        flow: String,
    ) {
        events.track(EVENT_VIP_VIEW, dims = mapOf(DIM_SOURCE to source, DIM_FLOW to flow))
    }

    override fun logCheckoutCanceled(
        plan: String,
        source: String,
        flow: String,
    ) {
        events.track(EVENT_VIP_CHECKOUT_CANCELED, dims = purchaseDims(plan, source, flow))
    }

    override fun logCheckoutFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        events.track(
            EVENT_VIP_CHECKOUT_FAILED,
            dims = purchaseDims(plan, source, flow) + (DIM_ERROR to error),
        )
    }

    override fun logConfirmed(
        plan: String,
        source: String,
        flow: String,
    ) {
        events.track(EVENT_VIP_CONFIRMED, dims = purchaseDims(plan, source, flow))
    }

    override fun logConfirmFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        events.track(
            EVENT_VIP_CONFIRM_FAILED,
            dims = purchaseDims(plan, source, flow) + (DIM_ERROR to error),
        )
    }

    private fun purchaseDims(
        plan: String,
        source: String,
        flow: String,
    ) = mapOf(DIM_PLAN to plan.lowercase(), DIM_SOURCE to source, DIM_FLOW to flow)
}
