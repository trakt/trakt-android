package tv.trakt.trakt.tv.core.streamings.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import tv.trakt.trakt.tv.core.streamings.model.StreamingSource.Companion
import tv.trakt.trakt.tv.networking.openapi.StreamingSourceDto

@Immutable
internal data class StreamingSource(
    val source: String,
    val name: String,
    val color: Color?,
    val images: Images,
) {
    @Immutable
    internal data class Images(
        val logo: String?,
        val channel: String?,
    )

    companion object
}

internal fun Companion.fromDto(dto: StreamingSourceDto): StreamingSource {
    return StreamingSource(
        source = dto.source,
        name = dto.name,
        color = runCatching { Color(dto.color.toColorInt()) }.getOrNull(),
        images = StreamingSource.Images(
            logo = dto.images.logo,
            channel = dto.images.channel,
        ),
    )
}
