package tv.trakt.trakt.common.firebase.analytics.implementation.firebase

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN
import com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.google.firebase.crashlytics.FirebaseCrashlytics
import tv.trakt.trakt.common.firebase.analytics.Analytics

// Android V3 events identifier
private const val EVENT_NAME_PREFIX = "av3_"

private const val EVENT_NAME_LIMIT = 40
private const val EVENT_NAME_ERROR = "Firebase event names must be less than 40 characters long"

private const val PARAMETER_MEDIA_TYPE = "media_type"
private const val PARAMETER_SOURCE = "source"
private const val PARAMETER_DATE = "date"
private const val PARAMETER_REACTION = "reaction"
private const val PARAMETER_RATING = "rating_value"
private const val PARAMETER_COUNT = "count"
private const val PARAMETER_RESULT = "result"
private const val PARAMETER_MODE = "mode"
private const val PARAMETER_PLAN = "plan"
private const val PARAMETER_CONTENT = "content_type"
private const val PARAMETER_TYPE = "type"
private const val PARAMETER_ACTION = "action"
private const val PARAMETER_STATE = "state"
private const val PARAMETER_REASON = "reason"
private const val PARAMETER_DURATION = "duration_ms"
private const val PARAMETER_FRAMES = "frames"
private const val PARAMETER_GROUPS = "groups"
private const val PARAMETER_FRAME = "frame"

private fun eventName(name: String) = "$EVENT_NAME_PREFIX$name"

private fun requireEventNames(vararg names: String) {
    names.forEach { require(eventName(it).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR } }
}

private fun Analytics.CommentContent.parameterValue(): String {
    return when (this) {
        Analytics.CommentContent.Text -> "text"
        Analytics.CommentContent.Gif -> "gif"
        Analytics.CommentContent.TextAndGif -> "text_and_gif"
    }
}

internal class FirebaseAnalytics(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val comments: Analytics.Comments,
    override val progress: Analytics.Progress,
    override val trivia: Analytics.Trivia,
    override val playback: Analytics.Playback,
    override val lists: Analytics.Lists,
    override val today: Analytics.Today,
) : Analytics {
    companion object Event {
        const val LOGOUT = "logout"
        const val MEDIA_MODE = "media_mode"
        const val MEDIA_MODE_CLICK = "media_mode_click"
        const val SHARE_ARRIVAL = "share_arrival"
        const val SEARCH = "search"
        const val VIP_UPGRADE = "vip_upgrade"
    }

    init {
        require(eventName(LOGOUT).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(SHARE_ARRIVAL).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        requireEventNames(SEARCH, VIP_UPGRADE)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
        userId?.let { firebaseCrashlytics.setUserId(it) }
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        firebaseAnalytics.setUserProperty(key, value)
    }

    override fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(
            eventName(SCREEN_VIEW),
            bundleOf(Param.SCREEN_NAME to screenName),
        )
    }

    override fun logUserLogin(source: String) {
        firebaseAnalytics.logEvent(
            eventName(LOGIN),
            bundleOf(PARAMETER_SOURCE to source.lowercase()),
        )
    }

    override fun logUserLogout() {
        firebaseAnalytics.logEvent(
            eventName(LOGOUT),
            null,
        )
    }

    override fun logMediaModeClick(mode: String) {
        firebaseAnalytics.logEvent(
            eventName(MEDIA_MODE_CLICK),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mode.lowercase(),
            ),
        )
    }

    override fun logMediaMode(mode: String) {
        firebaseAnalytics.logEvent(
            eventName(MEDIA_MODE),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mode.lowercase(),
            ),
        )
    }

    override fun logShareArrival(
        type: String,
        result: String,
    ) {
        firebaseAnalytics.logEvent(
            eventName(SHARE_ARRIVAL),
            bundleOf(
                PARAMETER_MEDIA_TYPE to type.lowercase(),
                PARAMETER_RESULT to result.lowercase(),
            ),
        )
    }

    override fun logSearch(mode: String) {
        firebaseAnalytics.logEvent(
            eventName(SEARCH),
            bundleOf(PARAMETER_MODE to mode.lowercase()),
        )
    }

    override fun logVipUpgrade(plan: String) {
        firebaseAnalytics.logEvent(
            eventName(VIP_UPGRADE),
            bundleOf(PARAMETER_PLAN to plan.lowercase()),
        )
    }
}

internal class FirebaseAnalyticsReactions(
    private val firebase: FirebaseAnalytics,
) : Analytics.Reactions {
    companion object Event {
        const val REACTIONS_ADD = "reactions_comment_add"
        const val REACTIONS_REMOVE = "reactions_comment_remove"
        const val MEDIA_REACTIONS_ADD = "reactions_media_add"
        const val MEDIA_REACTIONS_REMOVE = "reactions_media_remove"
    }

    init {
        require(eventName(REACTIONS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(REACTIONS_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(MEDIA_REACTIONS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(MEDIA_REACTIONS_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
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

    override fun logMediaReactionAdd(
        reaction: String,
        mediaType: String,
    ) {
        firebase.logEvent(
            eventName(MEDIA_REACTIONS_ADD),
            bundleOf(
                PARAMETER_REACTION to reaction.lowercase(),
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }

    override fun logMediaReactionRemove(
        reaction: String,
        mediaType: String,
    ) {
        firebase.logEvent(
            eventName(MEDIA_REACTIONS_REMOVE),
            bundleOf(
                PARAMETER_REACTION to reaction.lowercase(),
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }
}

internal class FirebaseAnalyticsRatings(
    private val firebase: FirebaseAnalytics,
) : Analytics.Ratings {
    companion object Event {
        const val RATINGS_ADD = "ratings_add"
        const val RATINGS_REMOVE = "ratings_remove"
        const val RATINGS_CHANGE = "ratings_change"
        const val FAVORITES_ADD = "favorites_add"
        const val FAVORITES_REMOVE = "favorites_remove"
    }

    init {
        require(eventName(RATINGS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(RATINGS_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(FAVORITES_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(FAVORITES_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        requireEventNames(RATINGS_CHANGE)
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

    override fun logRatingChange(
        rating: Int,
        mediaType: String,
    ) {
        firebase.logEvent(
            eventName(RATINGS_CHANGE),
            bundleOf(
                PARAMETER_RATING to rating,
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }

    override fun logRatingRemove(mediaType: String) {
        firebase.logEvent(
            eventName(RATINGS_REMOVE),
            bundleOf(
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

internal class FirebaseAnalyticsComments(
    private val firebase: FirebaseAnalytics,
) : Analytics.Comments {
    companion object Event {
        const val COMMENTS_ADD = "comments_add"
        const val COMMENTS_REMOVE = "comments_remove"
        const val COMMENTS_TRANSLATE = "comments_translate"
        const val COMMENTS_EDIT = "comments_edit"
    }

    init {
        require(eventName(COMMENTS_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(COMMENTS_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(COMMENTS_TRANSLATE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        requireEventNames(COMMENTS_EDIT)
    }

    override fun logCommentAdd(
        mediaType: String,
        content: Analytics.CommentContent,
    ) {
        firebase.logEvent(
            eventName(COMMENTS_ADD),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_CONTENT to content.parameterValue(),
            ),
        )
    }

    override fun logReplyAdd(content: Analytics.CommentContent) {
        firebase.logEvent(
            eventName(COMMENTS_ADD),
            bundleOf(PARAMETER_CONTENT to content.parameterValue()),
        )
    }

    override fun logCommentEdit(content: Analytics.CommentContent) {
        firebase.logEvent(
            eventName(COMMENTS_EDIT),
            bundleOf(PARAMETER_CONTENT to content.parameterValue()),
        )
    }

    override fun logCommentRemove() {
        firebase.logEvent(
            eventName(COMMENTS_REMOVE),
            null,
        )
    }

    override fun logReplyRemove() {
        firebase.logEvent(
            eventName(COMMENTS_REMOVE),
            null,
        )
    }

    override fun logCommentTranslate(characters: Int) {
        firebase.logEvent(
            eventName(COMMENTS_TRANSLATE),
            bundleOf(
                PARAMETER_COUNT to characters.toLong(),
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
        const val CHECKIN_START = "checkin_start"
        const val CHECKIN_STOP = "checkin_stop"
    }

    init {
        require(eventName(PROGRESS_WATCHED_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHED_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHLIST_ADD).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        require(eventName(PROGRESS_WATCHLIST_REMOVE).length <= EVENT_NAME_LIMIT) { EVENT_NAME_ERROR }
        requireEventNames(CHECKIN_START, CHECKIN_STOP)
    }

    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
        date: String?,
    ) {
        firebase.logEvent(
            eventName(PROGRESS_WATCHED_ADD),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
                PARAMETER_SOURCE to source.lowercase(),
                PARAMETER_DATE to (date ?: "now").lowercase(),
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

    override fun logCheckInStart(mediaType: String) {
        firebase.logEvent(
            eventName(CHECKIN_START),
            bundleOf(PARAMETER_MEDIA_TYPE to mediaType.lowercase()),
        )
    }

    override fun logCheckInStop(mediaType: String) {
        firebase.logEvent(
            eventName(CHECKIN_STOP),
            bundleOf(PARAMETER_MEDIA_TYPE to mediaType.lowercase()),
        )
    }
}

internal class FirebaseAnalyticsTrivia(
    private val firebase: FirebaseAnalytics,
) : Analytics.Trivia {
    companion object Event {
        const val TRIVIA_SCREEN = "trivia"
    }

    override fun logScreenView(source: String) {
        firebase.logEvent(
            eventName(TRIVIA_SCREEN),
            bundleOf(
                PARAMETER_SOURCE to source.lowercase(),
            ),
        )
    }
}

internal class FirebaseAnalyticsPlayback(
    private val firebase: FirebaseAnalytics,
) : Analytics.Playback {
    companion object Event {
        const val PLEX_PLAY_START = "plex_play_start"
        const val PLEX_PLAY_STOP = "plex_play_stop"
    }

    override fun logPlaybackStart(mediaType: String) {
        firebase.logEvent(
            eventName(PLEX_PLAY_START),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }

    override fun logPlaybackStop(mediaType: String) {
        firebase.logEvent(
            eventName(PLEX_PLAY_STOP),
            bundleOf(
                PARAMETER_MEDIA_TYPE to mediaType.lowercase(),
            ),
        )
    }
}

internal class FirebaseAnalyticsLists(
    private val firebase: FirebaseAnalytics,
) : Analytics.Lists {
    companion object Event {
        const val LISTS_ITEM_ADD = "lists_item_add"
        const val LISTS_ITEM_REMOVE = "lists_item_remove"
        const val LISTS_CREATE = "lists_create"
        const val LISTS_EDIT = "lists_edit"
        const val LISTS_DELETE = "lists_delete"
        const val LISTS_LIKE = "lists_like"
        const val LISTS_UNLIKE = "lists_unlike"
        const val SMART_LISTS_CREATE = "smart_lists_create"
        const val SMART_LISTS_DELETE = "smart_lists_delete"
    }

    init {
        requireEventNames(
            LISTS_ITEM_ADD,
            LISTS_ITEM_REMOVE,
            LISTS_CREATE,
            LISTS_EDIT,
            LISTS_DELETE,
            LISTS_LIKE,
            LISTS_UNLIKE,
            SMART_LISTS_CREATE,
            SMART_LISTS_DELETE,
        )
    }

    override fun logListItemAdd() = log(LISTS_ITEM_ADD)

    override fun logListItemRemove() = log(LISTS_ITEM_REMOVE)

    override fun logListCreate() = log(LISTS_CREATE)

    override fun logListEdit() = log(LISTS_EDIT)

    override fun logListDelete() = log(LISTS_DELETE)

    override fun logListLike(liked: Boolean) = log(if (liked) LISTS_LIKE else LISTS_UNLIKE)

    override fun logSmartListCreate() = log(SMART_LISTS_CREATE)

    override fun logSmartListDelete() = log(SMART_LISTS_DELETE)

    private fun log(name: String) {
        firebase.logEvent(eventName(name), null)
    }
}

internal class FirebaseAnalyticsToday(
    private val firebase: FirebaseAnalytics,
) : Analytics.Today {
    companion object Event {
        const val TODAY_OPEN = "today_open"
        const val TODAY_STORY = "today_story"
        const val TODAY_CLOSE = "today_close"
        const val TODAY_ACTION = "today_action"
    }

    init {
        requireEventNames(TODAY_OPEN, TODAY_STORY, TODAY_CLOSE, TODAY_ACTION)
    }

    override fun logOpen(
        source: String,
        hasUnseen: Boolean,
    ) {
        firebase.logEvent(
            eventName(TODAY_OPEN),
            bundleOf(
                PARAMETER_SOURCE to source,
                PARAMETER_STATE to if (hasUnseen) "unseen" else "seen",
            ),
        )
    }

    override fun logStory(type: String) {
        firebase.logEvent(
            eventName(TODAY_STORY),
            bundleOf(PARAMETER_TYPE to type),
        )
    }

    override fun logClose(
        reason: String,
        durationMs: Long,
        frames: Int,
        groups: Int,
    ) {
        firebase.logEvent(
            eventName(TODAY_CLOSE),
            bundleOf(
                PARAMETER_REASON to reason,
                PARAMETER_DURATION to durationMs,
                PARAMETER_FRAMES to frames,
                PARAMETER_GROUPS to groups,
            ),
        )
    }

    override fun logAction(
        action: String,
        frame: String,
    ) {
        firebase.logEvent(
            eventName(TODAY_ACTION),
            bundleOf(
                PARAMETER_ACTION to action,
                PARAMETER_FRAME to frame,
            ),
        )
    }
}
