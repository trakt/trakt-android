package tv.trakt.trakt.core.discover.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class DiscoverSection {
    TRENDING,
    POPULAR,
    ANTICIPATED,
    RECOMMENDED,
    ;

    @StringRes
    fun getTitle(): Int {
        return when (this) {
            TRENDING -> R.string.list_title_trending
            POPULAR -> R.string.list_title_most_popular
            ANTICIPATED -> R.string.list_title_most_anticipated
            RECOMMENDED -> R.string.list_title_recommended
        }
    }
}
