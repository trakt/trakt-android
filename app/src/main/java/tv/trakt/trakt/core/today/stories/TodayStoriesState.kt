package tv.trakt.trakt.core.today.stories

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.core.today.model.TodayStoryFrame
import tv.trakt.trakt.core.today.model.TodayStoryGroup

@Immutable
internal data class TodayStoryPosition(
    val groupKey: String? = null,
    val frameIndex: Int = 0,
)

@Immutable
internal data class TodayStoriesState(
    val groups: ImmutableList<TodayStoryGroup> = EmptyImmutableList,
    val position: TodayStoryPosition = TodayStoryPosition(),
    val loading: LoadingState = LoadingState.Idle,
) {
    /** Unknown keys fall back to the first group, matching web. */
    val groupIndex: Int = groups
        .indexOfFirst { it.key == position.groupKey }
        .coerceAtLeast(0)

    val group: TodayStoryGroup? = groups.getOrNull(groupIndex)

    val frameIndex: Int = position.frameIndex
        .coerceAtMost((group?.frames?.size ?: 1) - 1)
        .coerceAtLeast(0)

    val frame: TodayStoryFrame? = group?.frames?.getOrNull(frameIndex)
}
