package tv.trakt.trakt.common.core.klipy.data.remote

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.klipy.model.GifPage
import tv.trakt.trakt.common.core.klipy.model.GifsQuery
import tv.trakt.trakt.common.core.klipy.model.KlipyGif
import tv.trakt.trakt.common.core.klipy.model.KlipyGifFormats
import tv.trakt.trakt.common.core.klipy.model.KlipyGifMedia
import tv.trakt.trakt.common.core.klipy.model.KlipyGifRenditions
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyGifDto
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyGifFormatsDto
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyGifsRequest
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyMediaDto
import tv.trakt.trakt.common.networking.api.klipy.model.KlipyPageDto

/** KLIPY marks sponsored entries with a non-`gif` type; they are not playable GIFs. */
private const val KLIPY_TYPE_GIF = "gif"

internal fun GifsQuery.toRequest(): KlipyGifsRequest {
    return KlipyGifsRequest(
        page = pagination.page,
        perPage = pagination.limit,
        query = term,
        customerId = customerId,
        locale = locale.country.lowercase().ifEmpty { null },
        contentFilter = contentFilter.wireValue,
        formatFilter = formats.joinToString(separator = ",") { it.wireValue }.ifEmpty { null },
    )
}

internal fun KlipyPageDto<KlipyGifDto>.toDomain(): GifPage {
    return GifPage(
        items = data.mapNotNull { it.toDomain() }.toImmutableList(),
        page = currentPage,
        perPage = perPage,
        hasNext = hasNext,
    )
}

internal fun KlipyGifDto.toDomain(): KlipyGif? {
    if (id == null || slug.isNullOrBlank()) return null
    if (type != null && type != KLIPY_TYPE_GIF) return null

    val renditions = KlipyGifRenditions(
        hd = file?.hd.toDomain(),
        md = file?.md.toDomain(),
        sm = file?.sm.toDomain(),
        xs = file?.xs.toDomain(),
    )

    val gif = KlipyGif(
        id = id,
        slug = slug,
        title = title.orEmpty(),
        tags = tags.orEmpty().toImmutableList(),
        renditions = renditions,
        blurPreview = blurPreview?.ifBlank { null },
    )

    // Without a playable rendition there is nothing to show.
    return gif.takeIf { it.previewMedia != null }
}

private fun KlipyGifFormatsDto?.toDomain(): KlipyGifFormats? {
    if (this == null) return null

    return KlipyGifFormats(
        gif = gif.toDomain(),
        webp = webp.toDomain(),
        jpg = jpg.toDomain(),
        mp4 = mp4.toDomain(),
        webm = webm.toDomain(),
    )
}

private fun KlipyMediaDto?.toDomain(): KlipyGifMedia? {
    if (this == null || url.isNullOrBlank()) return null

    return KlipyGifMedia(
        url = url,
        width = width ?: 0,
        height = height ?: 0,
        sizeBytes = size ?: 0L,
    )
}
