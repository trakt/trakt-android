package tv.trakt.trakt.common.networking.api.v3.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class V3UsageResponse(
    val history: V3UsageItem,
    val ratings: V3UsageItem,
    @SerialName("watchlist_items")
    val watchlistItems: V3UsageItem,
    @SerialName("list_items")
    val listItems: V3UsageItem?,
    @SerialName("total_list_items")
    val totalListItems: V3UsageItem,
    @SerialName("static_lists")
    val staticLists: V3UsageItem,
    @SerialName("dynamic_lists")
    val dynamicLists: V3UsageItem,
    @SerialName("digital_library")
    val digitalLibrary: V3UsageItem,
    @SerialName("total_notes")
    val totalNotes: V3UsageItem,
) {
    @Immutable
    @Serializable
    data class V3UsageItem(
        val current: Int?,
        val free: Int?,
        val vip: Int?,
    )

    val isEmpty: Boolean
        get() = history.current == 0 &&
            ratings.current == 0 &&
            watchlistItems.current == 0 &&
            (listItems?.current ?: 0) == 0 &&
            totalListItems.current == 0 &&
            staticLists.current == 0 &&
            dynamicLists.current == 0 &&
            digitalLibrary.current == 0 &&
            totalNotes.current == 0
}
