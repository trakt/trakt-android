package tv.trakt.trakt.common.model.realitycheck

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.networking.api.v3.model.V3DocumentaryCheckResponse
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
data class DocumentaryCheck(
    val tier: DocumentaryTier,
    val summary: String,
    @Serializable(with = ImmutableListSerializer::class)
    val madeBy: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val fundedBy: ImmutableList<String>,
    @Serializable(with = ImmutableListSerializer::class)
    val disputes: ImmutableList<Dispute>,
) {
    @Immutable
    @Serializable
    data class Dispute(
        val claim: String,
        val by: String,
        val source: DisputeSource,
        val year: Int?,
        val reply: String?,
        val isMinor: Boolean,
    ) {
        companion object {
            fun fromDto(dto: V3DocumentaryCheckResponse.V3DocumentaryDispute): Dispute? {
                val source = DisputeSource.fromValue(dto.type) ?: return null
                return Dispute(
                    claim = dto.claim,
                    by = dto.by,
                    source = source,
                    year = dto.year,
                    reply = dto.reply,
                    isMinor = dto.impact == "minor",
                )
            }
        }
    }

    enum class DisputeSource(
        val value: String,
        val displayTextRes: Int,
    ) {
        Expert("expert", R.string.text_documentary_source_expert),
        Press("press", R.string.text_documentary_source_press),
        Interested("interested", R.string.text_documentary_source_interested),
        Filmmaker("filmmaker", R.string.text_documentary_source_filmmaker),
        ;

        companion object {
            fun fromValue(value: String?): DisputeSource? {
                return entries.firstOrNull { it.value == value }
            }
        }
    }

    companion object {
        fun fromDto(dto: V3DocumentaryCheckResponse): DocumentaryCheck? {
            if (!dto.applicable) return null
            val tier = DocumentaryTier.fromValue(dto.tier) ?: return null

            return DocumentaryCheck(
                tier = tier,
                summary = dto.summary.orEmpty(),
                madeBy = dto.makers?.madeBy.orEmpty().toImmutableList(),
                fundedBy = dto.makers?.fundedBy.orEmpty().toImmutableList(),
                disputes = dto.disputes.orEmpty()
                    .mapNotNull(Dispute::fromDto)
                    .sortedBy { it.isMinor }
                    .toImmutableList(),
            )
        }
    }
}

enum class DocumentaryTier(
    val value: String,
    val displayTextRes: Int,
    val segments: Int,
) {
    None("none", R.string.text_documentary_tier_none, 4),
    Minor("minor", R.string.text_documentary_tier_minor, 3),
    Central("central", R.string.text_documentary_tier_central, 2),
    Wide("wide", R.string.text_documentary_tier_wide, 1),
    NotEnough("not_enough", R.string.text_documentary_tier_not_enough, 0),
    ;

    companion object {
        fun fromValue(value: String?): DocumentaryTier? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
