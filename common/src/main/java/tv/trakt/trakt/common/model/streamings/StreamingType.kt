package tv.trakt.trakt.common.model.streamings

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class StreamingType(
    val order: Int,
    val type: String,
    @param:StringRes val labelRes: Int,
) {
    FAVORITE(0, "favorite", R.string.list_title_streaming_favorite),
    SUBSCRIPTION(1, "subscription", R.string.list_title_streaming_subscription),
    PURCHASE(2, "purchase", R.string.list_title_streaming_purchase),
    RENT(3, "rent", R.string.list_title_streaming_rent),
    FREE(4, "free", R.string.list_title_streaming_free),
}
