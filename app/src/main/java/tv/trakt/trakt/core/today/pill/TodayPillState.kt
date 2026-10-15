package tv.trakt.trakt.core.today.pill

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.core.today.model.TodayStoryGroup
import java.time.Instant

private const val MAX_POSTERS = 2

@Immutable
internal data class TodayPillState(
    val groups: ImmutableList<TodayStoryGroup> = EmptyImmutableList,
    val seenAt: ImmutableMap<String, Instant> = persistentMapOf(),
    val loading: LoadingState = LoadingState.Idle,
) {
    private val unseenGroups = groups.filterNot { it.isSeen(seenAt) }
    val unseenCount: Int = unseenGroups.size

    val isVisible: Boolean = groups.isNotEmpty()

    /** Group the viewer opens on: the first unseen one, else the newest. */
    val targetKey: String? = (unseenGroups.firstOrNull() ?: groups.firstOrNull())?.key

    val posters: ImmutableList<String> = groups
        .mapNotNull { it.media.images?.getPosterUrl() }
        .distinct()
        .take(MAX_POSTERS)
        .toImmutableList()
}
