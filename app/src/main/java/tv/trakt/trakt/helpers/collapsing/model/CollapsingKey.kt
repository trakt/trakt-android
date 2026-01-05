package tv.trakt.trakt.helpers.collapsing.model

internal enum class CollapsingKey(
    val preferenceKey: String,
) {
    DISCOVER_MEDIA_TRENDING("key_collapsing_discover_media_trending"),
    DISCOVER_MEDIA_POPULAR("key_collapsing_discover_media_popular"),
    DISCOVER_MEDIA_ANTICIPATED("key_collapsing_discover_media_anticipated"),
    DISCOVER_MEDIA_RECOMMENDED("key_collapsing_discover_media_recommended"),

    DISCOVER_SHOWS_TRENDING("key_collapsing_discover_shows_trending"),
    DISCOVER_SHOWS_POPULAR("key_collapsing_discover_shows_popular"),
    DISCOVER_SHOWS_ANTICIPATED("key_collapsing_discover_shows_anticipated"),
    DISCOVER_SHOWS_RECOMMENDED("key_collapsing_discover_shows_recommended"),

    DISCOVER_MOVIES_TRENDING("key_collapsing_discover_movies_trending"),
    DISCOVER_MOVIES_POPULAR("key_collapsing_discover_movies_popular"),
    DISCOVER_MOVIES_ANTICIPATED("key_collapsing_discover_movies_anticipated"),
    DISCOVER_MOVIES_RECOMMENDED("key_collapsing_discover_movies_recommended"),
}
