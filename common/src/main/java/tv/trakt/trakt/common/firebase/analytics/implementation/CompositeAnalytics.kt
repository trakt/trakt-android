package tv.trakt.trakt.common.firebase.analytics.implementation

import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class CompositeAnalytics(
    private val targets: List<Analytics>,
) : Analytics {
    override val reactions: Analytics.Reactions = CompositeReactions(targets.map(Analytics::reactions))
    override val ratings: Analytics.Ratings = CompositeRatings(targets.map(Analytics::ratings))
    override val comments: Analytics.Comments = CompositeComments(targets.map(Analytics::comments))
    override val progress: Analytics.Progress = CompositeProgress(targets.map(Analytics::progress))
    override val trivia: Analytics.Trivia = CompositeTrivia(targets.map(Analytics::trivia))
    override val playback: Analytics.Playback = CompositePlayback(targets.map(Analytics::playback))
    override val lists: Analytics.Lists = CompositeLists(targets.map(Analytics::lists))
    override val today: Analytics.Today = CompositeToday(targets.map(Analytics::today))
    override val vip: Analytics.Vip = CompositeVip(targets.map(Analytics::vip))

    override fun setUserId(userId: String?) {
        targets.forEach { it.setUserId(userId) }
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        targets.forEach { it.setUserProperty(key, value) }
    }

    override fun logScreenView(screenName: String) {
        targets.forEach { it.logScreenView(screenName) }
    }

    override fun logUserLogin(source: String) {
        targets.forEach { it.logUserLogin(source) }
    }

    override fun logUserLogout() {
        targets.forEach { it.logUserLogout() }
    }

    override fun logMediaModeClick(mode: String) {
        targets.forEach { it.logMediaModeClick(mode) }
    }

    override fun logMediaMode(mode: String) {
        targets.forEach { it.logMediaMode(mode) }
    }

    override fun logShareArrival(
        type: String,
        result: String,
    ) {
        targets.forEach { it.logShareArrival(type, result) }
    }

    override fun logSearch(mode: String) {
        targets.forEach { it.logSearch(mode) }
    }

    override fun logVipUpgrade(
        plan: String,
        source: String,
        flow: String,
    ) {
        targets.forEach { it.logVipUpgrade(plan, source, flow) }
    }

    override fun logVipUpsell(source: String) {
        targets.forEach { it.logVipUpsell(source) }
    }

    override fun logShare(
        source: String,
        type: String?,
    ) {
        targets.forEach { it.logShare(source, type) }
    }

    override fun logFollow(action: String) {
        targets.forEach { it.logFollow(action) }
    }

    override fun logBlock(blocked: Boolean) {
        targets.forEach { it.logBlock(blocked) }
    }

    override fun logTheme(theme: String) {
        targets.forEach { it.logTheme(theme) }
    }

    override fun logCoverImage(mediaType: String) {
        targets.forEach { it.logCoverImage(mediaType) }
    }

    override fun logCalendarPeriod(action: String) {
        targets.forEach { it.logCalendarPeriod(action) }
    }

    override fun logFilter(
        id: String,
        set: Boolean,
        mode: String,
    ) {
        targets.forEach { it.logFilter(id, set, mode) }
    }
}

private class CompositeReactions(
    private val targets: List<Analytics.Reactions>,
) : Analytics.Reactions {
    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) {
        targets.forEach { it.logReactionAdd(reaction, source) }
    }

    override fun logReactionRemove(source: String) {
        targets.forEach { it.logReactionRemove(source) }
    }

    override fun logMediaReactionAdd(
        reaction: String,
        mediaType: String,
    ) {
        targets.forEach { it.logMediaReactionAdd(reaction, mediaType) }
    }

    override fun logMediaReactionRemove(
        reaction: String,
        mediaType: String,
    ) {
        targets.forEach { it.logMediaReactionRemove(reaction, mediaType) }
    }
}

private class CompositeRatings(
    private val targets: List<Analytics.Ratings>,
) : Analytics.Ratings {
    override fun logRatingAdd(
        rating: Int,
        mediaType: String,
    ) {
        targets.forEach { it.logRatingAdd(rating, mediaType) }
    }

    override fun logRatingChange(
        rating: Int,
        mediaType: String,
    ) {
        targets.forEach { it.logRatingChange(rating, mediaType) }
    }

    override fun logRatingRemove(mediaType: String) {
        targets.forEach { it.logRatingRemove(mediaType) }
    }

    override fun logFavoriteAdd(mediaType: String) {
        targets.forEach { it.logFavoriteAdd(mediaType) }
    }

    override fun logFavoriteRemove(
        mediaType: String,
        source: String,
    ) {
        targets.forEach { it.logFavoriteRemove(mediaType, source) }
    }
}

private class CompositeComments(
    private val targets: List<Analytics.Comments>,
) : Analytics.Comments {
    override fun logCommentAdd(
        mediaType: String,
        content: Analytics.CommentContent,
    ) {
        targets.forEach { it.logCommentAdd(mediaType, content) }
    }

    override fun logCommentRemove() {
        targets.forEach { it.logCommentRemove() }
    }

    override fun logReplyAdd(content: Analytics.CommentContent) {
        targets.forEach { it.logReplyAdd(content) }
    }

    override fun logCommentEdit(content: Analytics.CommentContent) {
        targets.forEach { it.logCommentEdit(content) }
    }

    override fun logReplyRemove() {
        targets.forEach { it.logReplyRemove() }
    }

    override fun logCommentTranslate(characters: Int) {
        targets.forEach { it.logCommentTranslate(characters) }
    }
}

private class CompositeProgress(
    private val targets: List<Analytics.Progress>,
) : Analytics.Progress {
    override fun logAddWatchedMedia(
        mediaType: String,
        source: String,
        date: String?,
    ) {
        targets.forEach { it.logAddWatchedMedia(mediaType, source, date) }
    }

    override fun logRemoveWatchedMedia(
        mediaType: String,
        source: String,
    ) {
        targets.forEach { it.logRemoveWatchedMedia(mediaType, source) }
    }

    override fun logAddWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        targets.forEach { it.logAddWatchlistMedia(mediaType, source) }
    }

    override fun logRemoveWatchlistMedia(
        mediaType: String,
        source: String,
    ) {
        targets.forEach { it.logRemoveWatchlistMedia(mediaType, source) }
    }

    override fun logCheckInStart(mediaType: String) {
        targets.forEach { it.logCheckInStart(mediaType) }
    }

    override fun logCheckInStop(mediaType: String) {
        targets.forEach { it.logCheckInStop(mediaType) }
    }

    override fun logDrop(mediaType: String) {
        targets.forEach { it.logDrop(mediaType) }
    }

    override fun logHideRecommendation(mediaType: String) {
        targets.forEach { it.logHideRecommendation(mediaType) }
    }
}

private class CompositeTrivia(
    private val targets: List<Analytics.Trivia>,
) : Analytics.Trivia {
    override fun logScreenView(source: String) {
        targets.forEach { it.logScreenView(source) }
    }
}

private class CompositePlayback(
    private val targets: List<Analytics.Playback>,
) : Analytics.Playback {
    override fun logPlaybackStart(mediaType: String) {
        targets.forEach { it.logPlaybackStart(mediaType) }
    }

    override fun logPlaybackStop(mediaType: String) {
        targets.forEach { it.logPlaybackStop(mediaType) }
    }

    override fun logTrailer(slug: String) {
        targets.forEach { it.logTrailer(slug) }
    }

    override fun logExtras(
        slug: String,
        type: String,
    ) {
        targets.forEach { it.logExtras(slug, type) }
    }

    override fun logStreamOn(source: String) {
        targets.forEach { it.logStreamOn(source) }
    }
}

private class CompositeLists(
    private val targets: List<Analytics.Lists>,
) : Analytics.Lists {
    override fun logListItemAdd() {
        targets.forEach { it.logListItemAdd() }
    }

    override fun logListItemRemove() {
        targets.forEach { it.logListItemRemove() }
    }

    override fun logListCreate() {
        targets.forEach { it.logListCreate() }
    }

    override fun logListEdit() {
        targets.forEach { it.logListEdit() }
    }

    override fun logListDelete() {
        targets.forEach { it.logListDelete() }
    }

    override fun logListLike(liked: Boolean) {
        targets.forEach { it.logListLike(liked) }
    }

    override fun logSmartListCreate() {
        targets.forEach { it.logSmartListCreate() }
    }

    override fun logSmartListDelete() {
        targets.forEach { it.logSmartListDelete() }
    }

    override fun logListSort(
        sortBy: String,
        sortHow: String,
    ) {
        targets.forEach { it.logListSort(sortBy, sortHow) }
    }
}

private class CompositeToday(
    private val targets: List<Analytics.Today>,
) : Analytics.Today {
    override fun logOpen(
        source: String,
        hasUnseen: Boolean,
    ) {
        targets.forEach { it.logOpen(source, hasUnseen) }
    }

    override fun logStory(type: String) {
        targets.forEach { it.logStory(type) }
    }

    override fun logClose(
        reason: String,
        durationMs: Long,
        frames: Int,
        groups: Int,
    ) {
        targets.forEach {
            it.logClose(
                reason = reason,
                durationMs = durationMs,
                frames = frames,
                groups = groups,
            )
        }
    }

    override fun logAction(
        action: String,
        frame: String,
    ) {
        targets.forEach { it.logAction(action, frame) }
    }
}

private class CompositeVip(
    private val targets: List<Analytics.Vip>,
) : Analytics.Vip {
    override fun logUpsellImpression(source: String) {
        targets.forEach { it.logUpsellImpression(source) }
    }

    override fun logView(
        source: String,
        flow: String,
    ) {
        targets.forEach { it.logView(source, flow) }
    }

    override fun logCheckoutCanceled(
        plan: String,
        source: String,
        flow: String,
    ) {
        targets.forEach { it.logCheckoutCanceled(plan, source, flow) }
    }

    override fun logCheckoutFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        targets.forEach { it.logCheckoutFailed(plan, source, flow, error) }
    }

    override fun logConfirmed(
        plan: String,
        source: String,
        flow: String,
    ) {
        targets.forEach { it.logConfirmed(plan, source, flow) }
    }

    override fun logConfirmFailed(
        plan: String,
        source: String,
        flow: String,
        error: String,
    ) {
        targets.forEach { it.logConfirmFailed(plan, source, flow, error) }
    }
}
