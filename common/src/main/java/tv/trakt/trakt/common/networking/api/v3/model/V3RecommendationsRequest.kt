package tv.trakt.trakt.common.networking.api.v3.model

data class V3RecommendationsRequest(
    val limit: Int,
    val extended: String? = null,
    val watchWindow: Int? = null,
    val watchnow: String? = null,
    val genres: String? = null,
    val subgenres: String? = null,
    val years: String? = null,
    val ratings: String? = null,
    val runtimes: String? = null,
    val certifications: String? = null,
    val countries: String? = null,
    val ignoreWatched: Boolean? = null,
    val ignoreWatchlisted: Boolean? = null,
    val ignoreCollected: Boolean? = null,
)
