package tv.trakt.trakt.core.summary.history.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal enum class MediaHistoryType(
    val value: String,
) {
    Movie("movie"),
    Show("show"),
    Episode("episode"),
}
