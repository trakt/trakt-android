package tv.trakt.trakt.common.networking.api.scrobble.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.openapitools.client.models.PostSyncRatingsRemoveRequestMoviesInner

@Serializable
data class PostScrobbleMovieRequest(
    @SerialName(value = "progress")
    val progress: Float,
    @SerialName(value = "movie")
    val movie: PostSyncRatingsRemoveRequestMoviesInner,
)
