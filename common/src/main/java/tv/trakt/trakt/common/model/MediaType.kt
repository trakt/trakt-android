package tv.trakt.trakt.common.model

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Keep
@Immutable
@Serializable
enum class MediaType(
    val value: String,
) {
    Movie("movie"),
    Show("show"),
    Season("season"),
    Episode("episode"),
}
