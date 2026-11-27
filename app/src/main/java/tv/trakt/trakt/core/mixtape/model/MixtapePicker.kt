package tv.trakt.trakt.core.mixtape.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Fit
import tv.trakt.trakt.common.model.mixtape.MixtapeItem

@Immutable
internal data class MixtapePicker(
    val kind: Kind = Kind.Recent,
    val items: ImmutableList<Entry> = persistentListOf(),
) {
    enum class Kind {
        Recent,
        Fits,
        Search,
    }

    @Immutable
    data class Entry(
        val item: MixtapeItem,
        val isOff: Boolean = false,
    )

    companion object {
        fun create(
            history: List<MixtapeItem>,
            searchResults: List<MixtapeItem>,
            known: Map<TraktId, MixtapeItem>,
            picks: List<TraktId>,
            fits: List<Fit>,
            query: String,
        ): MixtapePicker {
            val unpicked = history.filterNot { it.id in picks }
            val needle = query.trim()

            if (needle.isNotEmpty()) {
                val fitIds = fits.mapTo(mutableSetOf()) { it.id }
                val recentMatches = unpicked.filter { it.title.contains(needle, ignoreCase = true) }
                val olderMatches = searchResults.filter {
                    it.id !in picks && it.title.contains(needle, ignoreCase = true)
                }
                val items = (recentMatches + olderMatches)
                    .distinctBy { it.id }
                    .map { Entry(item = it, isOff = picks.isNotEmpty() && it.id !in fitIds) }
                    .sortedBy { it.isOff }
                return MixtapePicker(kind = Kind.Search, items = items.toImmutableList())
            }

            if (picks.isEmpty()) {
                return MixtapePicker(
                    kind = Kind.Recent,
                    items = unpicked.map { Entry(item = it) }.toImmutableList(),
                )
            }

            return MixtapePicker(
                kind = Kind.Fits,
                items = fits
                    .filterNot { it.id in picks }
                    .mapNotNull { fit -> known[fit.id]?.let { Entry(item = it) } }
                    .toImmutableList(),
            )
        }
    }
}
