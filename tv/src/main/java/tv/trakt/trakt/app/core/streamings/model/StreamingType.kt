package tv.trakt.trakt.app.core.streamings.model

import androidx.annotation.StringRes
import tv.trakt.trakt.app.R

internal enum class StreamingType(
    val order: Int,
    val type: String,
    @param:StringRes val labelRes: Int,
) {
    FAVORITE(0, "favorite", R.string.header_streaming_favorite),
    SUBSCRIPTION(1, "subscription", R.string.header_streaming_subscriptions),
    PURCHASE(2, "purchase", R.string.header_streaming_purchase),
    RENT(3, "rent", R.string.header_streaming_rent),
    FREE(4, "free", R.string.header_streaming_free),
}
