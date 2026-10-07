package tv.trakt.trakt.common.core.klipy.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.CommentGif

@Immutable
data class KlipyGif(
    val id: Long,
    val slug: String,
    val title: String,
    val tags: ImmutableList<String>,
    val renditions: KlipyGifRenditions,
    val blurPreview: String?,
) {
    val previewMedia: KlipyGifMedia?
        get() = renditions.sm?.animated ?: renditions.xs?.animated ?: renditions.md?.animated

    val fullMedia: KlipyGifMedia?
        get() = renditions.md?.animated ?: renditions.hd?.animated ?: renditions.sm?.animated

    fun toCommentGif(): CommentGif? {
        val sizes = listOfNotNull(renditions.md, renditions.hd, renditions.sm)
        val media = sizes.firstNotNullOfOrNull { it.webp } ?: return null
        return CommentGif(
            slug = slug,
            url = media.url,
        )
    }
}

@Immutable
data class KlipyGifRenditions(
    val hd: KlipyGifFormats?,
    val md: KlipyGifFormats?,
    val sm: KlipyGifFormats?,
    val xs: KlipyGifFormats?,
)

@Immutable
data class KlipyGifFormats(
    val gif: KlipyGifMedia?,
    val webp: KlipyGifMedia?,
    val jpg: KlipyGifMedia?,
    val mp4: KlipyGifMedia?,
    val webm: KlipyGifMedia?,
) {
    val animated: KlipyGifMedia?
        get() = webp ?: gif
}

@Immutable
data class KlipyGifMedia(
    val url: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
)
