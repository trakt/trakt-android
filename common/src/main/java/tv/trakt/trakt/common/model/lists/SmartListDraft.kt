package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import tv.trakt.trakt.common.networking.SmartListDraftDto

@Immutable
data class SmartListDraft(
    val filters: SmartListFilters,
    val confidence: Float,
) {
    @Composable
    fun rememberSummary(): String {
        val resources = LocalResources.current
        val configuration = LocalConfiguration.current

        return remember(this, configuration) {
            val labels = listOf(
                resources.getString(filters.source.displayRes),
                resources.getString(filters.media.recipeLabelRes),
            ) + filters.toSummaryParts(resources, appLocale())

            labels.joinToString(" · ")
        }
    }

    companion object {
        fun fromDto(dto: SmartListDraftDto): SmartListDraft {
            return SmartListDraft(
                filters = SmartListFilters.fromDto(
                    source = dto.source,
                    mediaType = dto.mediaType,
                    filters = dto.filters,
                ),
                confidence = dto.confidence.toFloat(),
            )
        }
    }
}
