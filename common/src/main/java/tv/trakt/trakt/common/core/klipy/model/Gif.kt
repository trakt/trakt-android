package tv.trakt.trakt.common.core.klipy.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@JvmInline
value class GifId(
    val value: Long,
)

@Immutable
data class Gif(
    val id: GifId,
    val slug: String,
    val title: String,
    val tags: ImmutableList<String>,
    val renditions: GifRenditions,
    /** Base64 data URI KLIPY ships for a blurred placeholder while the rendition loads. */
    val blurPreview: String?,
) {
    val preview: GifMedia?
        get() = renditions.sm?.animated ?: renditions.xs?.animated ?: renditions.md?.animated

    val full: GifMedia?
        get() = renditions.md?.animated ?: renditions.hd?.animated ?: preview

    val shareUrl: String?
        get() = (renditions.md?.gif ?: renditions.hd?.gif ?: renditions.sm?.gif ?: full)?.url
}

@Immutable
data class GifRenditions(
    val hd: GifFormats?,
    val md: GifFormats?,
    val sm: GifFormats?,
    val xs: GifFormats?,
)

@Immutable
data class GifFormats(
    val gif: GifMedia?,
    val webp: GifMedia?,
    val jpg: GifMedia?,
    val mp4: GifMedia?,
    val webm: GifMedia?,
) {
    /** WebP first - same animation at a fraction of the GIF payload. */
    val animated: GifMedia?
        get() = webp ?: gif
}

@Immutable
data class GifMedia(
    val url: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
)
