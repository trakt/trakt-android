package tv.trakt.trakt.analytics

/**
 * Analytics interface for logging events.
 */
interface Analytics {
    fun logScreenView(
        screenName: String,
        screenClass: String,
    )

    fun logUserLogin()

    fun logUserLogout()
}
