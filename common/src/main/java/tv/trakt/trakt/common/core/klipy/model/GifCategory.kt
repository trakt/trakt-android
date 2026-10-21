package tv.trakt.trakt.common.core.klipy.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.data.dto.KlipyCategoryDto

@Immutable
data class GifCategory(
    val title: String,
    val query: String,
    val previewUrl: String,
) {
    companion object {
        fun fromDto(dto: KlipyCategoryDto): GifCategory {
            return GifCategory(
                title = dto.category,
                query = dto.query,
                previewUrl = dto.previewUrl,
            )
        }
    }
}
