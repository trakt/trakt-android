package tv.trakt.trakt.app.core.discover.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

@Keep
internal enum class DiscoverMediaSection(
    @param:StringRes val titleRes: Int,
    val screenName: String,
) {
    Trending(R.string.list_title_trending, "all_discover_trending"),
    Popular(R.string.list_title_most_popular, "all_discover_popular"),
    Anticipated(R.string.list_title_most_anticipated, "all_discover_anticipated"),
}
