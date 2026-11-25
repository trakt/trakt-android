package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.data.dto.MediaMatchDto
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer

@Immutable
@Serializable
data class MediaMatch(
    val score: Int?,
    @Serializable(ImmutableListSerializer::class)
    val taste: ImmutableList<Taste> = persistentListOf(),
) {
    val isLow: Boolean
        get() = score != null && score < LOW_THRESHOLD

    @Immutable
    @Serializable
    data class Taste(
        val slug: String,
        val label: String,
    )

    companion object {
        const val LOW_THRESHOLD = 40

        fun fromDto(dto: MediaMatchDto): MediaMatch {
            return MediaMatch(
                score = dto.match?.coerceIn(0, 100),
                taste = dto.taste.orEmpty()
                    .map { Taste(slug = it.slug, label = it.label) }
                    .toImmutableList(),
            )
        }
    }
}
