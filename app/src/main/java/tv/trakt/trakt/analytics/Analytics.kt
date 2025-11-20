package tv.trakt.trakt.analytics

import tv.trakt.trakt.core.main.model.MediaMode

/**
 * Analytics interface for logging events.
 */
interface Analytics {
    val reactions: Reactions
    val ratings: Ratings
    val progress: Progress

    /**
     * Logs a screen view event.
     */
    fun logScreenView(screenName: String)

    /**
     * Logs a user login event.
     */
    fun logUserLogin()

    /**
     * Logs a user logout event.
     */
    fun logUserLogout()

    /**
     * Logs a click on the media mode.
     */
    fun logMediaModeClick(mode: MediaMode)

    /**
     * Logs the current media mode.
     */
    fun logMediaMode(mode: MediaMode)

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
}
