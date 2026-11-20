package tv.trakt.trakt.core.lists.features.smart.search

import androidx.annotation.Keep
import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

@Keep
internal enum class SmartListSearchKind(
    @param:StringRes val headerRes: Int,
    @param:StringRes val placeholderRes: Int,
) {
    Keywords(R.string.header_smart_list_keywords, R.string.input_placeholder_search_keywords),
    People(R.string.header_smart_list_people, R.string.input_placeholder_search_people),
    Studios(R.string.header_smart_list_studios, R.string.input_placeholder_search_studios),
    Networks(R.string.header_smart_list_networks, R.string.input_placeholder_search_networks),
}
