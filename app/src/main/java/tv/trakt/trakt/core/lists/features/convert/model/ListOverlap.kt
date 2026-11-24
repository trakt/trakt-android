package tv.trakt.trakt.core.lists.features.convert.model

import androidx.compose.runtime.Immutable

private const val MAX_CHECKED_TITLES = 100

@Immutable
internal data class ListOverlap(
    val kept: Int,
    val total: Int,
) {
    companion object {
        fun from(
            count: Int,
            confidence: Float,
        ): ListOverlap? {
            val total = count.coerceAtMost(MAX_CHECKED_TITLES)
            if (total <= 0) return null

            val kept = Math.round(confidence.coerceIn(0F, 1F) * total)
            return ListOverlap(kept = kept, total = total)
        }
    }
}
