package tv.trakt.trakt.core.userprofile.sections.match.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

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
