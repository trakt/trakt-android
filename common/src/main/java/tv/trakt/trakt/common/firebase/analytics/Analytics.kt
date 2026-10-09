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
        fun logCommentAdd(mediaType: String)

        /**
         * Logs the removal of a comment.
         */
        fun logCommentRemove()

        /**
         * Logs the addition of a reply to a comment.
         */
        fun logReplyAdd()

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
