package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant

/** A title's story: the frames the viewer pages through before moving to the next title. */
@Immutable
internal data class TodayStoryGroup(
    val story: TodayTitleStory,
    val frames: ImmutableList<TodayStoryFrame>,
) {
    val key: String
        get() = story.key

    /** Seen only when viewed after its latest activity, so new activity resurfaces it. */
    fun isSeen(seenAt: Map<String, Instant>): Boolean {
        val seen = seenAt[key] ?: return false
        return !seen.isBefore(story.latestAt)
    }

    companion object {
        fun fromStory(story: TodayTitleStory): TodayStoryGroup {
            val friendFrames = story.actions.map { TodayStoryFrame.Friend(story, it) }
            val frames = when {
                story.users.size < 2 -> friendFrames
                else -> persistentListOf(TodayStoryFrame.Summary(story)) + friendFrames
            }

            return TodayStoryGroup(
                story = story,
                frames = frames.toImmutableList(),
            )
        }
    }
}
