package tv.trakt.trakt.core.share.model

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.resources.R

internal enum class ShareImageVariant(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    Default("open-graph", R.string.share_variant_default),
    Feed("feed", R.string.share_variant_feed),
    Story("story", R.string.share_variant_story),
    ;

    fun getImageUri(
        mediaType: String,
        mediaSlug: String,
    ): Uri {
        return "${WEB_V3_BASE_URL}api/shareable-image?type=$mediaType&slug=$mediaSlug&variant=$value"
            .toUri()
    }
}
