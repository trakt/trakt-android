package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SmartListItemDto
import tv.trakt.trakt.common.networking.SmartListTemplateDto

@Immutable
data class SmartListTemplate(
    val id: String,
    val filters: SmartListFilters,
    val personal: Boolean,
    val total: Int?,
    val images: Images,
) {
    companion object {
        fun fromDto(dto: SmartListTemplateDto): SmartListTemplate {
            return SmartListTemplate(
                id = dto.id,
                filters = SmartListFilters.fromDto(
                    source = dto.source,
                    mediaType = dto.mediaType,
                    filters = dto.filters,
                ),
                personal = dto.personal,
                total = dto.total,
                images = imagesFromItems(dto.items),
            )
        }

        fun imagesFromItems(items: List<SmartListItemDto>): Images {
            return Images(
                posters = items
                    .mapNotNull { item ->
                        when {
                            item.show != null -> item.show?.let { Images.forShow(TraktId(it.ids.trakt)) }
                            item.movie != null -> item.movie?.let { Images.forMovie(TraktId(it.ids.trakt)) }
                            else -> null
                        }?.poster?.firstOrNull()
                    }
                    .distinct()
                    .toImmutableList(),
            )
        }
    }
}
