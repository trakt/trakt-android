package tv.trakt.trakt.common.firebase.analytics.implementation.hal

private val ROUTE_INVALID_CHARACTERS = Regex("[^a-z0-9_.-]")

private val WEB_ROUTES = mapOf(
    "home" to "/home",
    "discover" to "/discover",
    "all_discover_trending" to "/discover/trending",
    "all_discover_popular" to "/discover/popular",
    "all_discover_anticipated" to "/discover/anticipated",
    "all_discover_recommended" to "/discover/recommended",
    "all_discover_releases" to "/discover/releases",
    "all_up_next" to "/users/[user]/progress",
    "all_watchlist" to "/users/[user]/watchlist",
    "all_library" to "/users/[user]/library",
    "lists" to "/users/[user]/lists",
    "all_lists" to "/users/[user]/lists",
    "all_personal_list" to "/users/[user]/lists/[list]",
    "all_activity_personal" to "/history",
    "all_activity_social" to "/social/activity",
    "all_favorites" to "/profile/[slug]/favorites",
    "profile" to "/profile/me",
    "movie_details" to "/movies/[slug]",
    "show_details" to "/shows/[slug]",
    "episode_details" to "/shows/[slug]/seasons/[season]/episodes/[episode]",
    "person_details" to "/people/[slug]",
    "search" to "/search",
    "settings" to "/settings",
    "billing" to "/vip",
    "today_stories" to "/today",
    "game" to "/play/rated-higher",
)

internal fun halRoute(screenName: String): String {
    val normalized = screenName.lowercase()
    return WEB_ROUTES[normalized] ?: ("/" + normalized.replace(ROUTE_INVALID_CHARACTERS, "_"))
}
