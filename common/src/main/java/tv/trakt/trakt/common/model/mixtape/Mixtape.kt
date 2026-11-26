package tv.trakt.trakt.common.model.mixtape

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.dto.MixtapeDto
import tv.trakt.trakt.common.model.TraktId
import kotlin.math.roundToInt

@Immutable
data class Mixtape(
    val fits: ImmutableList<Fit> = persistentListOf(),
    val suggestions: ImmutableList<Suggestion> = persistentListOf(),
) {
    @Immutable
    data class Fit(
        val id: TraktId,
        val score: Double,
        val item: MixtapeItem?,
    )

    @Immutable
    data class Suggestion(
        val item: MixtapeItem,
        val because: TraktId,
        val match: Int,
    )

    companion object {
        fun fromDto(dto: MixtapeDto): Mixtape {
            return Mixtape(
                fits = dto.fits
                    .map {
                        Fit(
                            id = TraktId(it.id),
                            score = it.score,
                            item = MixtapeItem.fromDto(it),
                        )
                    }
                    .toImmutableList(),
                suggestions = dto.suggestions
                    .mapNotNull { suggestion ->
                        val item = MixtapeItem.fromDto(suggestion) ?: return@mapNotNull null
                        Suggestion(
                            item = item,
                            because = TraktId(suggestion.because),
                            match = suggestion.match.roundToInt(),
                        )
                    }
                    .toImmutableList(),
            )
        }
    }
}
