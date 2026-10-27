package tv.trakt.trakt.common.core.streamings.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.streamings.StreamingType
import tv.trakt.trakt.common.model.streamings.StreamingType.Free

/**
 * Streaming sources grouped by the way they are watched (favorite, subscription, purchase, …).
 */
@Immutable
data class AllStreamingsSection(
    val type: StreamingType,
    val rows: ImmutableList<StreamingServiceRow>,
)

/**
 * Adds an empty [Free] section when [needed] and none exists, so free entries that are not
 * streaming services (a YouTube special) have a section to render in.
 */
fun ImmutableList<AllStreamingsSection>.withFreeSection(needed: Boolean): ImmutableList<AllStreamingsSection> {
    if (!needed || any { it.type == Free }) return this

    return (this + AllStreamingsSection(type = Free, rows = EmptyImmutableList))
        .sortedBy { it.type.order }
        .toImmutableList()
}
