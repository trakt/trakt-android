package tv.trakt.trakt.core.profile.sections.progress.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal data class ProgressPage(
    val items: ImmutableList<ProfileProgressItem>,
    val fetched: Int,
)

internal fun ImmutableList<ProfileProgressItem>.asPage(): ProgressPage {
    return ProgressPage(items = this, fetched = size)
}

internal fun ImmutableList<ProfileProgressItem>.pageOfEnded(ended: Boolean): ProgressPage {
    return ProgressPage(
        items = filterIsInstance<ProfileProgressItem.ShowItem>()
            .filter { it.show.isEnded == ended }
            .toImmutableList(),
        fetched = size,
    )
}
