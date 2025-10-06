package tv.trakt.trakt.common.networking.helpers

interface CacheMarkerProvider {
    suspend fun getMarker(): String

    suspend fun invalidate()
}
