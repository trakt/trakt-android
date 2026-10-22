package tv.trakt.trakt.common.firebase.analytics.implementation.debug

import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class DebugAnalytics(
    override val reactions: Analytics.Reactions,
    override val ratings: Analytics.Ratings,
    override val comments: Analytics.Comments,
    override val progress: Analytics.Progress,
    override val trivia: Analytics.Trivia,
    override val playback: Analytics.Playback,
    override val lists: Analytics.Lists,
    override val today: Analytics.Today,
    override val vip: Analytics.Vip,
) : Analytics {
    override fun setUserId(userId: String?) {
        Timber.d("setUserId: $userId")
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        Timber.d("setUserProperty: key=$key, value=$value")
    }

    override fun logScreenView(screenName: String) {
        Timber.d("logScreenView: screenName=$screenName")
    }

    override fun logUserLogin(source: String) {
        Timber.d("logUserLogin: source=${source.lowercase()}")
    }

    override fun logUserLogout() {
        Timber.d("logUserLogout")
    }

    override fun logMediaMode(mode: String) {
        Timber.d("logMediaMode: mode=${mode.lowercase()}")
    }

    override fun logMediaModeClick(mode: String) {
        Timber.d("logMediaModeClick: mode=${mode.lowercase()}")
    }

    override fun logShareArrival(
        type: String,
        result: String,
    ) {
        Timber.d("logShareArrival: type=%s, result=%s", type, result)
    }

    override fun logSearch(mode: String) {
        Timber.d("logSearch: mode=%s", mode)
    }

    override fun logVipUpgrade(
        plan: String,
        source: String,
        flow: String,
    ) {
        Timber.d("logVipUpgrade: plan=%s, source=%s, flow=%s", plan, source, flow)
    }

    override fun logVipUpsell(source: String) {
        Timber.d("logVipUpsell: source=%s", source)
    }

    override fun logShare(
        source: String,
        type: String?,
    ) {
        Timber.d("logShare: source=%s, type=%s", source, type)
    }

    override fun logFollow(action: String) {
        Timber.d("logFollow: action=%s", action)
    }

    override fun logBlock(blocked: Boolean) {
        Timber.d("logBlock: blocked=%s", blocked)
    }

    override fun logTheme(theme: String) {
        Timber.d("logTheme: theme=%s", theme)
    }

    override fun logCoverImage(mediaType: String) {
        Timber.d("logCoverImage: mediaType=%s", mediaType)
    }

    override fun logSettings(settings: String) {
        Timber.d("logSettings: settings=%s", settings)
    }

    override fun logCalendarPeriod(action: String) {
        Timber.d("logCalendarPeriod: action=%s", action)
    }

    override fun logFilter(
        id: String,
        set: Boolean,
        mode: String,
    ) {
        Timber.d("logFilter: id=%s, set=%s, mode=%s", id, set, mode)
    }

    override fun logLoginStart(source: String) {
        Timber.d("logLoginStart: source=%s", source)
    }

    override fun logLoginFailed(error: String) {
        Timber.d("logLoginFailed: error=%s", error)
    }

    override fun logStreamingService(
        action: String,
        service: String,
    ) {
        Timber.d("logStreamingService: action=%s, service=%s", action, service)
    }

    override fun logMuteSharer() {
        Timber.d("logMuteSharer")
    }

    override fun logReportOpen(type: String) {
        Timber.d("logReportOpen: type=%s", type)
    }

    override fun logReport(
        type: String,
        reason: String,
        outcome: String,
    ) {
        Timber.d("logReport: type=%s, reason=%s, outcome=%s", type, reason, outcome)
    }

    override fun logDrilldown(
        source: String,
        type: String?,
    ) {
        Timber.d("logDrilldown: source=%s, type=%s", source, type)
    }

    override fun logLink(
        source: String,
        target: String,
    ) {
        Timber.d("logLink: source=%s, target=%s", source, target)
    }

    override fun logBannerDismiss(
        id: String,
        value: String,
    ) {
        Timber.d("logBannerDismiss: id=%s, value=%s", id, value)
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

    override fun logMediaReactionAdd(
        reaction: String,
        mediaType: String,
    ) {
        Timber.d("logMediaReactionAdd: reaction=${reaction.lowercase()}, mediaType=${mediaType.lowercase()}")
    }

    override fun logMediaReactionRemove(
        reaction: String,
        mediaType: String,
    ) {
        Timber.d("logMediaReactionRemove: reaction=${reaction.lowercase()}, mediaType=${mediaType.lowercase()}")
    }
}

internal class DebugAnalyticsRatings : Analytics.Ratings {
    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) {
        Timber.d("logRatingAdd: rating=$rating, mediaType=${mediaType.lowercase()}")
    }

    override fun logRatingRemove(mediaType: String) {
        Timber.d("logRatingRemove: mediaType=${mediaType.lowercase()}")
    }

    override fun logRatingChange(
        rating: Int,
        mediaType: String,
    ) {
        Timber.d("logRatingChange: rating=%d, mediaType=%s", rating, mediaType.lowercase())
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
    override fun logCommentAdd(
        mediaType: String,
        content: Analytics.CommentContent,
    ) {
        Timber.d("logCommentAdd: mediaType=%s, content=%s", mediaType.lowercase(), content)
    }

    override fun logCommentRemove() {
        Timber.d("logCommentRemove")
    }

    override fun logReplyAdd(content: Analytics.CommentContent) {
        Timber.d("logReplyAdd: content=%s", content)
    }

    override fun logCommentEdit(content: Analytics.CommentContent) {
        Timber.d("logCommentEdit: content=%s", content)
    }

    override fun logReplyRemove() {
        Timber.d("logReplyRemove")
    }

    override fun logCommentTranslate(characters: Int) {
        Timber.d("logCommentTranslate: characters=%d", characters)
    }

    override fun logSpoilerReveal() {
        Timber.d("logSpoilerReveal")
    }

    override fun logGifPicker(
        action: String,
        source: String,
    ) {
        Timber.d("logGifPicker: action=%s, source=%s", action, source)
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

    override fun logCheckInStart(mediaType: String) {
        Timber.d("logCheckInStart: mediaType=%s", mediaType)
    }

    override fun logCheckInStop(mediaType: String) {
        Timber.d("logCheckInStop: mediaType=%s", mediaType)
    }

    override fun logDrop(mediaType: String) {
        Timber.d("logDrop: mediaType=%s", mediaType)
    }

    override fun logHideRecommendation(mediaType: String) {
        Timber.d("logHideRecommendation: mediaType=%s", mediaType)
    }
}

internal class DebugAnalyticsTrivia : Analytics.Trivia {
    override fun logScreenView(source: String) {
        Timber.d("logTriviaScreenView: source=$source")
    }
}

internal class DebugAnalyticsPlayback : Analytics.Playback {
    override fun logPlaybackStart(mediaType: String) {
        Timber.d("logPlaybackStart: mediaType=${mediaType.lowercase()}")
    }

    override fun logPlaybackStop(mediaType: String) {
        Timber.d("logPlaybackStop: mediaType=${mediaType.lowercase()}")
    }

    override fun logTrailer(slug: String) {
        Timber.d("logTrailer: slug=%s", slug)
    }

    override fun logExtras(
        slug: String,
        type: String,
    ) {
        Timber.d("logExtras: slug=%s, type=%s", slug, type)
    }

    override fun logStreamOn(source: String) {
        Timber.d("logStreamOn: source=%s", source)
    }
}

internal class DebugAnalyticsLists : Analytics.Lists {
    override fun logListItemAdd() {
        Timber.d("logListItemAdd")
    }

    override fun logListItemRemove() {
        Timber.d("logListItemRemove")
    }

    override fun logListCreate() {
        Timber.d("logListCreate")
    }

    override fun logListEdit() {
        Timber.d("logListEdit")
    }

    override fun logListDelete() {
        Timber.d("logListDelete")
    }

    override fun logListLike(liked: Boolean) {
        Timber.d("logListLike: liked=%s", liked)
    }

    override fun logSmartListCreate() {
        Timber.d("logSmartListCreate")
    }

    override fun logSmartListDelete() {
        Timber.d("logSmartListDelete")
    }

    override fun logListSort(
        sortBy: String,
        sortHow: String,
    ) {
        Timber.d("logListSort: sortBy=%s, sortHow=%s", sortBy, sortHow)
    }
}

internal class DebugAnalyticsToday : Analytics.Today {
    override fun logOpen(
        source: String,
        hasUnseen: Boolean,
    ) {
        Timber.d("logTodayOpen: source=%s, hasUnseen=%s", source, hasUnseen)
    }

    override fun logStory(type: String) {
        Timber.d("logTodayStory: type=%s", type)
    }

    override fun logClose(
        reason: String,
        durationMs: Long,
        frames: Int,
        groups: Int,
    ) {
        Timber.d("logTodayClose: reason=%s, durationMs=%d, frames=%d, groups=%d", reason, durationMs, frames, groups)
    }

    override fun logAction(
        action: String,
        frame: String,
    ) {
        Timber.d("logTodayAction: action=%s, frame=%s", action, frame)
    }
}

internal class DebugAnalyticsVip : Analytics.Vip {
    override fun logUpsellImpression(source: String) {
        Timber.d("logVipUpsellImpression: source=%s", source)
    }

    override fun logView(
        source: String,
        flow: String,
    ) {
        Timber.d("logVipView: source=%s, flow=%s", source, flow)
    }

    override fun logCheckoutCanceled(
        plan: String,
        source: String,
        flow: String,
    ) {
        Timber.d("logVipCheckoutCanceled: plan=%s, source=%s, flow=%s", plan, source, flow)
    }

    override fun logCheckoutFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        Timber.d("logVipCheckoutFailed: plan=%s, source=%s, flow=%s, error=%s", plan, source, flow, error)
    }

    override fun logConfirmed(
        plan: String,
        source: String,
        flow: String,
    ) {
        Timber.d("logVipConfirmed: plan=%s, source=%s, flow=%s", plan, source, flow)
    }

    override fun logConfirmFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        Timber.d("logVipConfirmFailed: plan=%s, source=%s, flow=%s, error=%s", plan, source, flow, error)
    }
}
