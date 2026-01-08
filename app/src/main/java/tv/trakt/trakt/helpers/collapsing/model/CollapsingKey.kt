package tv.trakt.trakt.helpers.collapsing.model

internal enum class CollapsingKey(
    val preferenceKey: String,
) {
    // Home Screen
    HOME_MEDIA_UP_NEXT("key_home_media_up_next"),
    HOME_MEDIA_START_WATCHING("key_home_media_start_watching"),
    HOME_MEDIA_UPCOMING("key_home_media_personal"),
    HOME_MEDIA_HISTORY("key_home_media_history"),
    HOME_MEDIA_SOCIAL("key_home_media_social"),

    HOME_SHOWS_UP_NEXT("key_home_shows_up_next"),
    HOME_SHOWS_START_WATCHING("key_home_shows_start_watching"),
    HOME_SHOWS_UPCOMING("key_home_shows_personal"),
    HOME_SHOWS_HISTORY("key_home_shows_history"),
    HOME_SHOWS_SOCIAL("key_home_shows_social"),

    HOME_MOVIES_UP_NEXT("key_home_movies_up_next"),
    HOME_MOVIES_START_WATCHING("key_home_movies_start_watching"),
    HOME_MOVIES_UPCOMING("key_home_movies_personal"),
    HOME_MOVIES_HISTORY("key_home_movies_history"),
    HOME_MOVIES_SOCIAL("key_home_movies_social"),

    // Discover Screen
    DISCOVER_MEDIA_TRENDING("key_discover_media_trending"),
    DISCOVER_MEDIA_POPULAR("key_discover_media_popular"),
    DISCOVER_MEDIA_ANTICIPATED("key_discover_media_anticipated"),
    DISCOVER_MEDIA_RECOMMENDED("key_discover_media_recommended"),

    DISCOVER_SHOWS_TRENDING("key_discover_shows_trending"),
    DISCOVER_SHOWS_POPULAR("key_discover_shows_popular"),
    DISCOVER_SHOWS_ANTICIPATED("key_discover_shows_anticipated"),
    DISCOVER_SHOWS_RECOMMENDED("key_discover_shows_recommended"),

    DISCOVER_MOVIES_TRENDING("key_discover_movies_trending"),
    DISCOVER_MOVIES_POPULAR("key_discover_movies_popular"),
    DISCOVER_MOVIES_ANTICIPATED("key_discover_movies_anticipated"),
    DISCOVER_MOVIES_RECOMMENDED("key_discover_movies_recommended"),

    // Profile Screen
    PROFILE_HISTORY("key_profile_history"),
    PROFILE_FAVORITES("key_profile_favorites"),
    PROFILE_LIBRARY("key_profile_library"),
}
