package tv.trakt.trakt.core.userprofile.sections.match.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

private const val UNICORN_RARITY_THRESHOLD = 0.5F

internal enum class MatchBand {
    High,
    Mid,
    Low,
    ;

    companion object {
        fun of(score: Int): MatchBand {
            return when {
                score >= 70 -> High
                score >= 40 -> Mid
                else -> Low
            }
        }
    }
}

internal enum class MatchChipTier {
    Unicorn,
    Rare,
    Notable,
    Common,
    ;

    val hasSparkle: Boolean
        get() = this == Unicorn || this == Rare

    companion object {
        fun of(
            index: Int,
            rarity: Float?,
        ): MatchChipTier {
            if (rarity != null && rarity >= UNICORN_RARITY_THRESHOLD) return Unicorn
            if (index <= 0) return Rare
            if (index <= 2) return Notable
            return Common
        }
    }
}

internal enum class MatchLevel(
    @StringRes val labelRes: Int,
    @StringRes val sublabelRes: Int,
) {
    Soulmates(R.string.match_label_soulmates, R.string.match_sublabel_soulmates),
    CoStars(R.string.match_label_co_stars, R.string.match_sublabel_co_stars),
    Crossover(R.string.match_label_crossover, R.string.match_sublabel_crossover),
    SpinOff(R.string.match_label_spin_off, R.string.match_sublabel_spin_off),
    Cameo(R.string.match_label_cameo, R.string.match_sublabel_cameo),
    DifferentUniverse(R.string.match_label_different_universe, R.string.match_sublabel_different_universe),
    ;

    companion object {
        fun of(score: Int): MatchLevel {
            return when {
                score >= 90 -> Soulmates
                score >= 75 -> CoStars
                score >= 60 -> Crossover
                score >= 45 -> SpinOff
                score >= 25 -> Cameo
                else -> DifferentUniverse
            }
        }
    }
}
