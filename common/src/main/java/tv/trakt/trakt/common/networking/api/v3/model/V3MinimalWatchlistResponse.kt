package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3MinimalWatchlistResponse(
    val shows: List<Int>?,
    val movies: List<Int>?,
)
