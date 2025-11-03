package tv.trakt.trakt.analytics

/**
 * Analytics interface for logging events.
 */
interface Analytics {
    val reactions: Reactions
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
