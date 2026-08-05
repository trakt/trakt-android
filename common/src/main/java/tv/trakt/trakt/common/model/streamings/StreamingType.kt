package tv.trakt.trakt.common.model.streamings

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class StreamingType(
    val order: Int,
    val type: String,
    @param:StringRes val labelRes: Int,
) {
    Favorite(0, "favorite", R.string.list_title_streaming_favorite),
    Subscription(1, "subscription", R.string.list_title_streaming_subscription),
    Purchase(2, "purchase", R.string.list_title_streaming_purchase),
    Rent(3, "rent", R.string.list_title_streaming_rent),
    Free(4, "free", R.string.list_title_streaming_free),
}
