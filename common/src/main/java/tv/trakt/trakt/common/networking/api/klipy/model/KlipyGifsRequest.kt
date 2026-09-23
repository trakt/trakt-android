package tv.trakt.trakt.common.networking.api.klipy.model

/**
 * Query parameters shared by the KLIPY trending and search endpoints. Null values are dropped
 * by Ktor, so KLIPY falls back to its own defaults.
 */
data class KlipyGifsRequest(
    val page: Int,
    val perPage: Int,
    val query: String? = null,
    val customerId: String? = null,
    val locale: String? = null,
    val contentFilter: String? = null,
    val formatFilter: String? = null,
)
