package tv.trakt.trakt.common.model.realitycheck

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.common.networking.api.v3.model.V3RealityCheckResponse
import tv.trakt.trakt.resources.R

private const val STORY_SUBJECT = "story"

@Immutable
@Serializable
data class RealityCheck(
    val verdict: RealityTier,
    val story: RealityTier,
    @Serializable(with = ImmutableListSerializer::class)
    val protagonists: ImmutableList<Protagonist>,
    val summary: String,
    val detail: Detail?,
) {
    val hasUnclearTier: Boolean
        get() = story == RealityTier.Unclear || protagonists.any { it.tier == RealityTier.Unclear }

    @Immutable
    @Serializable
    data class Protagonist(
        val name: String,
        val tier: RealityTier,
    )

    @Immutable
    @Serializable
    data class Detail(
        val breakdown: String,
        @Serializable(with = ImmutableListSerializer::class)
        val claims: ImmutableList<Claim>,
        @Serializable(with = ImmutableListSerializer::class)
        val characters: ImmutableList<Character>,
        val timeline: String,
        @Serializable(with = ImmutableListSerializer::class)
        val leftOut: ImmutableList<String>,
    )

    @Immutable
    @Serializable
    data class Claim(
        val about: String?,
        val onScreen: String,
        val documented: String,
        val label: ClaimLabel,
        val isMinor: Boolean,
    ) {
        companion object {
            fun fromDto(dto: V3RealityCheckResponse.V3RealityClaim): Claim? {
                val label = ClaimLabel.fromValue(dto.label) ?: return null
                return Claim(
                    about = dto.about?.takeUnless { it.trim().equals(STORY_SUBJECT, ignoreCase = true) },
                    onScreen = dto.onScreen,
                    documented = dto.documented,
                    label = label,
                    isMinor = dto.impact == "minor",
                )
            }
        }
    }

    @Immutable
    @Serializable
    data class Character(
        val name: String,
        val kind: CharacterKind,
        val note: String,
    ) {
        companion object {
            fun fromDto(dto: V3RealityCheckResponse.V3RealityCharacter): Character? {
                val kind = CharacterKind.fromValue(dto.kind) ?: return null
                return Character(
                    name = dto.name,
                    kind = kind,
                    note = dto.note.orEmpty(),
                )
            }
        }
    }

    enum class ClaimLabel(
        val value: String,
        val displayTextRes: Int,
    ) {
        Accurate("accurate", R.string.text_reality_claim_accurate),
        Compressed("compressed", R.string.text_reality_claim_compressed),
        Composite("composite", R.string.text_reality_claim_composite),
        Changed("changed", R.string.text_reality_claim_changed),
        Invented("invented", R.string.text_reality_claim_invented),
        ;

        companion object {
            fun fromValue(value: String?): ClaimLabel? {
                return entries.firstOrNull { it.value == value }
            }
        }
    }

    enum class CharacterKind(
        val value: String,
        val displayTextRes: Int,
    ) {
        Real("real", R.string.text_reality_kind_real),
        Composite("composite", R.string.text_reality_kind_composite),
        Invented("invented", R.string.text_reality_kind_invented),
        ;

        companion object {
            fun fromValue(value: String?): CharacterKind? {
                return entries.firstOrNull { it.value == value }
            }
        }
    }

    companion object {
        fun fromDto(dto: V3RealityCheckResponse): RealityCheck? {
            if (!dto.applicable) return null

            return RealityCheck(
                verdict = RealityTier.fromValue(dto.verdict),
                story = RealityTier.fromValue(dto.story),
                protagonists = dto.protagonists.orEmpty()
                    .map {
                        Protagonist(
                            name = it.name,
                            tier = RealityTier.fromValue(it.tier),
                        )
                    }
                    .toImmutableList(),
                summary = dto.summary.orEmpty(),
                detail = detailFromDto(dto),
            )
        }

        private fun detailFromDto(dto: V3RealityCheckResponse): Detail? {
            val breakdown = dto.breakdown ?: return null

            return Detail(
                breakdown = breakdown,
                claims = dto.claims.orEmpty()
                    .mapNotNull(Claim::fromDto)
                    .toImmutableList(),
                characters = dto.characters.orEmpty()
                    .mapNotNull(Character::fromDto)
                    .toImmutableList(),
                timeline = dto.timeline.orEmpty(),
                leftOut = dto.leftOut.orEmpty().toImmutableList(),
            )
        }
    }
}

enum class RealityTier(
    val value: String,
    val displayTextRes: Int,
    val segments: Int,
) {
    Faithful("faithful", R.string.text_reality_tier_faithful, 4),
    MostlyTrue("mostly_true", R.string.text_reality_tier_mostly_true, 3),
    Loose("loose", R.string.text_reality_tier_loose, 2),
    InNameOnly("in_name_only", R.string.text_reality_tier_in_name_only, 1),
    Unclear("unclear", R.string.text_reality_tier_unclear, 0),
    ;

    companion object {
        const val SEGMENT_COUNT = 4

        fun fromValue(value: String?): RealityTier {
            return entries.firstOrNull { it.value == value } ?: Unclear
        }
    }
}
