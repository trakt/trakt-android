package tv.trakt.trakt.core.profile.sections.progress.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A page of progress items together with how many items the endpoint actually returned.
 *
 * [items] may be shorter than [fetched] when the caller splits a bucket by show status.
 * Paging has to key off [fetched], since a page that filters down to fewer items than the
 * page limit still means there is another page behind it.
 */
internal data class ProgressPage(
    val items: ImmutableList<ProfileProgressItem>,
    val fetched: Int,
)

internal fun ImmutableList<ProfileProgressItem>.asPage(): ProgressPage {
    return ProgressPage(items = this, fetched = size)
}

/**
 * Splits the completed bucket on show status. The up next endpoint has no status filter,
 * so a show that ended and a show the user is merely caught up with arrive together and
 * are told apart here.
 */
internal fun ImmutableList<ProfileProgressItem>.pageOfEnded(ended: Boolean): ProgressPage {
    return ProgressPage(
        items = filterIsInstance<ProfileProgressItem.ShowItem>()
            .filter { it.show.hasEnded == ended }
            .toImmutableList(),
        fetched = size,
    )
}
