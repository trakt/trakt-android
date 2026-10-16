package tv.trakt.trakt.common.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Keep
@Immutable
@Serializable
enum class ShowEnding(
    val slug: String,
    @param:StringRes val displayStringRes: Int,
) {
    Concluded("concluded", R.string.translated_value_ending_complete),
    Open("open", R.string.translated_value_ending_unresolved),
    ;

    companion object {
        fun fromSlug(value: String?): ShowEnding? = entries.firstOrNull { it.slug.equals(value, ignoreCase = true) }
    }
}
