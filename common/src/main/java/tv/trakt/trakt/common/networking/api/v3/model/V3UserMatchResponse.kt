package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3UserMatchResponse(
    val score: Int,
    val breakdown: V3Breakdown,
    val shared: V3Shared,
) {
    @Immutable
    @Serializable
    data class V3Breakdown(
        val subgenres: Int,
        val favorites: Int,
    )

    @Immutable
    @Serializable
    data class V3Shared(
        val subgenres: List<V3SharedSubgenre>,
        val favorites: V3SharedFavorites,
    )

    @Immutable
    @Serializable
    data class V3SharedSubgenre(
        val id: Int,
        val name: String,
        val slug: String,
        val watchCount: Int,
        val rarity: Float? = null,
    )

    @Immutable
    @Serializable
    data class V3SharedFavorites(
        val movies: List<Int>,
        val shows: List<Int>,
    )
}
