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
    MOVIE("movie"),
    SHOW("show"),
    SEASON("season"),
    EPISODE("episode"),
}
