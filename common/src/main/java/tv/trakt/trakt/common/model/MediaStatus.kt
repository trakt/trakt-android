package tv.trakt.trakt.common.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Keep
@Immutable
@Serializable
enum class MediaStatus(
    val slug: String,
    @param:StringRes val displayStringRes: Int,
) {
    Canceled("canceled", R.string.translated_value_status_canceled),
    Continuing("continuing", R.string.translated_value_status_continuing),
    Ended("ended", R.string.translated_value_status_ended),
    InProduction("in production", R.string.translated_value_status_in_production),
    Pilot("pilot", R.string.translated_value_status_pilot),
    Planned("planned", R.string.translated_value_status_planned),
    PostProduction("post production", R.string.translated_value_status_post_production),
    Released("released", R.string.translated_value_status_released),
    ReturningSeries("returning series", R.string.translated_value_status_returning_series),
    Rumored("rumored", R.string.translated_value_status_rumored),
    Unknown("unknown", R.string.translated_value_status_unknown),
    Upcoming("upcoming", R.string.translated_value_status_upcoming),
    ;

    companion object {
fun fromSlug(value: String?): MediaStatus? = entries.firstOrNull { it.slug.equals(value, ignoreCase = true) }
    }
}
