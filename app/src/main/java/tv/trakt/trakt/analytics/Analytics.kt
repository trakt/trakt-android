package tv.trakt.trakt.analytics

/**
 * Analytics interface for logging events.
 */
interface Analytics {
    val reactions: Reactions
    val ratings: Ratings
    val progress: Progress

    fun logScreenView(screenName: String)

    fun logUserLogin()

    fun logUserLogout()

    interface Reactions {
        fun logReactionAdd(
            reaction: String,
            source: String,
        )

        fun logReactionRemove(source: String)
    }

    interface Ratings {
        fun logRatingAdd(
            rating: Int,
            mediaType: String,
        )

        fun logFavoriteAdd(mediaType: String)

        fun logFavoriteRemove(mediaType: String)
    }

    interface Progress {
        fun logAddWatchedMedia(
            mediaType: String,
            source: String,
        )

        fun logAddWatchlistMedia(
            mediaType: String,
            source: String,
        )
    }
}
