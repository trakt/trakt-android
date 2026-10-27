package tv.trakt.trakt.core.userprofile.sections.match.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

// Values from 0.5 up mean the topic is rare for both movies and shows, not just one side.
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

/**
 * Visual weight of a shared topic chip. The server orders topics by distinctiveness, so the
 * position sets the tier unless the topic is globally rare.
 */
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

@StringRes
internal fun matchLabelRes(score: Int): Int {
    return when {
        score >= 90 -> R.string.match_label_soulmates
        score >= 75 -> R.string.match_label_co_stars
        score >= 60 -> R.string.match_label_crossover
        score >= 45 -> R.string.match_label_spin_off
        score >= 25 -> R.string.match_label_cameo
        else -> R.string.match_label_different_universe
    }
}
