package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SmartListDraftsSummaryDto

@Immutable
data class SmartListDraftsSummary(
    val convertibleCount: Int,
    val bestConfidence: Float?,
    val lists: ImmutableList<Entry>,
) {
    @Immutable
    data class Entry(
        val ids: Ids,
        val name: String,
        val itemCount: Int,
        val confidence: Float,
    )

    companion object {
        fun fromDto(dto: SmartListDraftsSummaryDto): SmartListDraftsSummary {
            return SmartListDraftsSummary(
                convertibleCount = dto.convertibleCount,
                bestConfidence = dto.bestConfidence?.toFloat(),
                lists = dto.lists
                    .map {
                        Entry(
                            ids = Ids(
                                trakt = TraktId(it.ids.trakt),
                                slug = SlugId(it.ids.slug),
                            ),
                            name = it.name ?: it.ids.slug,
                            itemCount = it.itemCount ?: 0,
                            confidence = it.confidence.toFloat(),
                        )
                    }
                    .toImmutableList(),
            )
        }
    }
}
