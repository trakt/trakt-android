package tv.trakt.trakt.core.discover.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

@Keep
internal enum class DiscoverSection {
    Trending,
    Popular,
    Anticipated,
    Recommended,
    ;

    @StringRes
    fun getTitle(): Int {
        return when (this) {
            Trending -> R.string.list_title_trending
            Popular -> R.string.list_title_most_popular
            Anticipated -> R.string.list_title_most_anticipated
            Recommended -> R.string.list_title_recommended
        }
    }
}
