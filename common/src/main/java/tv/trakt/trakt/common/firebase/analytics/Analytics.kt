package tv.trakt.trakt.common.firebase.analytics

/**
 * Analytics interface for logging events.
 */
interface Analytics {
    val reactions: Reactions
    val ratings: Ratings
    val comments: Comments
    val progress: Progress
    val trivia: Trivia
    val playback: Playback
    val lists: Lists
    val today: Today

    /**
     * Sets the user ID for analytics.
     */
    fun setUserId(userId: String?)

    /**
     * Sets a user property for analytics.
     */
    fun setUserProperty(
        key: String,
        value: String?,
    )

    /**
     * Logs a screen view event.
     */
    fun logScreenView(screenName: String)

    /**
     * Logs a user login event.
     * @param source The redirect that delivered the authorization code.
     */
    fun logUserLogin(source: String)

    /**
     * Logs a user logout event.
     */
    fun logUserLogout()

    /**
     * Logs a click on the media mode.
     */
    fun logMediaModeClick(mode: String)

    /**
     * Logs the current media mode.
     */
    fun logMediaMode(mode: String)

    /**
     * Logs the arrival on a shared link and whether the sharer was credited.
     * @param type The shared item type (movie, show, person).
     * @param result The credit result (recorded, duplicate, anonymous, uncredited, ...).
     */
    fun logShareArrival(
        type: String,
        result: String,
    )

    /**
     * Logs a search that returned results.
     * @param mode The searched catalogue (media, show, movie, people, lists).
     */
    fun logSearch(mode: String)

    /**
     * Logs the start of a VIP purchase flow.
     * @param plan The plan duration (monthly, yearly, two_years).
     */
    fun logVipUpgrade(plan: String)

    enum class CommentContent {
        Text,
        Gif,
        TextAndGif,
    }

    interface Trivia {
        /**
         * Logs a screen view event for trivia screens.
         */
        fun logScreenView(source: String)
    }

    interface Reactions {
        /**
         * Logs the addition of a reaction.
         */
        fun logReactionAdd(
            reaction: String,
            source: String,
        )

        /**
         * Logs the removal of a reaction.
         */
        fun logReactionRemove(source: String)

        /**
         * Logs the addition of a reaction to a movie, show, season or episode.
         */
        fun logMediaReactionAdd(
            reaction: String,
            mediaType: String,
        )

        /**
         * Logs the removal of a reaction from a movie, show, season or episode.
         */
        fun logMediaReactionRemove(
            reaction: String,
            mediaType: String,
        )
    }

    interface Ratings {
        /**
         * Logs the addition of a rating.
         */
        fun logRatingAdd(
            rating: Int,
            mediaType: String,
        )

        /**
         * Logs the removal of a rating.
         */
        fun logRatingRemove(mediaType: String)

        /**
         * Logs replacing an existing rating with a new value.
         */
        fun logRatingChange(
            rating: Int,
            mediaType: String,
        )

        /**
         * Logs the removal of a favorite media.
         */
        fun logFavoriteAdd(mediaType: String)

        /**
         * Logs the removal of a favorite media.
         */
        fun logFavoriteRemove(
            mediaType: String,
            source: String,
        )
    }

    interface Comments {
        /**
         * Logs the addition of a comment.
         */
        fun logCommentAdd(
            mediaType: String,
            content: CommentContent,
        )

        /**
         * Logs the removal of a comment.
         */
        fun logCommentRemove()

        /**
         * Logs the addition of a reply to a comment.
         */
        fun logReplyAdd(content: CommentContent)

        /**
         * Logs editing an existing comment or reply.
         */
        fun logCommentEdit(content: CommentContent)

        /**
         * Logs the removal of a reply to a comment.
         */
        fun logReplyRemove()

        /**
         * Logs a comment or reply being translated, on device or in Google Translate.
         * @param characters The number of characters sent for translation.
         */
        fun logCommentTranslate(characters: Int)
    }

    interface Progress {
        /**
         * Logs adding media to watched history.
         */
        fun logAddWatchedMedia(
            mediaType: String,
            source: String,
            date: String?,
        )

        /**
         * Logs removing media from watched history.
         */
        fun logRemoveWatchedMedia(
            mediaType: String,
            source: String,
        )

        /**
         * Logs adding media to watchlist.
         */
        fun logAddWatchlistMedia(
            mediaType: String,
            source: String,
        )

        /**
         * Logs removing media from watchlist.
         */
        fun logRemoveWatchlistMedia(
            mediaType: String,
            source: String,
        )

        /**
         * Logs starting a check-in.
         * @param mediaType The checked-in media type (movie, episode).
         */
        fun logCheckInStart(mediaType: String)

        /**
         * Logs stopping an active check-in.
         * @param mediaType The checked-in media type (movie, episode).
         */
        fun logCheckInStop(mediaType: String)
    }

    interface Lists {
        /**
         * Logs adding a media item to a personal list.
         */
        fun logListItemAdd()

        /**
         * Logs removing a media item from a personal list.
         */
        fun logListItemRemove()

        /**
         * Logs creating a personal list.
         */
        fun logListCreate()

        /**
         * Logs editing a personal list.
         */
        fun logListEdit()

        /**
         * Logs deleting a personal list.
         */
        fun logListDelete()

        /**
         * Logs liking or unliking a list.
         */
        fun logListLike(liked: Boolean)

        /**
         * Logs creating a smart list.
         */
        fun logSmartListCreate()

        /**
         * Logs deleting a smart list.
         */
        fun logSmartListDelete()
    }

    interface Today {
        /**
         * Logs opening the Today stories viewer.
         * @param source Where the viewer was opened from (navbar, overview, link).
         * @param hasUnseen Whether any story was unseen when opened.
         */
        fun logOpen(
            source: String,
            hasUnseen: Boolean,
        )

        /**
         * Logs entering a story group.
         * @param type The group type (for-you, title).
         */
        fun logStory(type: String)

        /**
         * Logs closing the Today stories viewer.
         * @param reason Why it closed (close, end, leave).
         * @param durationMs How long the viewer was open.
         * @param frames Distinct frames shown.
         * @param groups Distinct groups shown.
         */
        fun logClose(
            reason: String,
            durationMs: Long,
            frames: Int,
            groups: Int,
        )

        /**
         * Logs an action taken on a story frame.
         * @param action The action (watchlist, mark-as-watched, view-all).
         * @param frame The frame type (for-you, summary, friend).
         */
        fun logAction(
            action: String,
            frame: String,
        )
    }

    interface Playback {
        /**
         * Logs the start of Plex Play media playback.
         */
        fun logPlaybackStart(mediaType: String)

        /**
         * Logs the end of Plex Play media playback.
         */
        fun logPlaybackStop(mediaType: String)
    }
}
