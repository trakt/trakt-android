package tv.trakt.trakt.core.calendar.model

internal enum class CalendarSource(
    val analyticsSource: String,
    val screenName: String?,
    val requiresAuth: Boolean,
    val showWatchlistAction: Boolean,
    val showDropAction: Boolean,
) {
    Personal(
        analyticsSource = "calendar",
        screenName = null,
        requiresAuth = true,
        showWatchlistAction = false,
        showDropAction = true,
    ),
    Releases(
        analyticsSource = "releases",
        screenName = "all_discover_releases",
        requiresAuth = false,
        showWatchlistAction = true,
        showDropAction = false,
    ),
}
