package tv.trakt.trakt.common.networking.api.klipy.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class KlipyGifDto(
    val id: Long? = null,
    val slug: String? = null,
    val title: String? = null,
    val type: String? = null,
    val file: KlipyGifFileDto? = null,
    val tags: List<String>? = null,
    @SerialName("blur_preview")
    val blurPreview: String? = null,
)

/**
 * Rendition buckets, largest to smallest. See https://docs.klipy.com/gifs-api/gifs-format-sizes.
 */
@Immutable
@Serializable
data class KlipyGifFileDto(
    val hd: KlipyGifFormatsDto? = null,
    val md: KlipyGifFormatsDto? = null,
    val sm: KlipyGifFormatsDto? = null,
    val xs: KlipyGifFormatsDto? = null,
)

@Immutable
@Serializable
data class KlipyGifFormatsDto(
    val gif: KlipyMediaDto? = null,
    val webp: KlipyMediaDto? = null,
    val jpg: KlipyMediaDto? = null,
    val mp4: KlipyMediaDto? = null,
    val webm: KlipyMediaDto? = null,
)

@Immutable
@Serializable
data class KlipyMediaDto(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
)
