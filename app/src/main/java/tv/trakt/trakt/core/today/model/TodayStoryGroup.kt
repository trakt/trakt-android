package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant

private const val FOR_YOU_KEY = "for-you"

@Immutable
internal sealed interface TodayStoryGroup {
    val key: String
    val frames: ImmutableList<TodayStoryFrame>

    val media: TodayMedia

    val latestAt: Instant

    /** Seen only when viewed after its latest activity, so new activity resurfaces it. */
    fun isSeen(seenAt: Map<String, Instant>): Boolean {
        val seen = seenAt[key] ?: return false
        return !seen.isBefore(latestAt)
    }

    @Immutable
    data class Title(
        val story: TodayTitleStory,
        override val frames: ImmutableList<TodayStoryFrame>,
    ) : TodayStoryGroup {
        override val key = story.key
        override val media = story.media
        override val latestAt = story.latestAt
    }

    @Immutable
    data class ForYou(
        val items: ImmutableList<TodayForYouItem>,
    ) : TodayStoryGroup {
        override val key = FOR_YOU_KEY
        override val frames: ImmutableList<TodayStoryFrame> = items
            .map(TodayStoryFrame::ForYou)
            .toImmutableList()
        override val media = items.first().media
        override val latestAt: Instant = items.maxOf { it.releasedAt }
    }

    companion object {
        fun from(
            forYou: List<TodayForYouItem>,
            stories: List<TodayTitleStory>,
        ): ImmutableList<TodayStoryGroup> {
            val titleGroups = stories.map(::fromStory)
            if (forYou.isEmpty()) return titleGroups.toImmutableList()

            return (listOf(ForYou(forYou.toImmutableList())) + titleGroups).toImmutableList()
        }

        private fun fromStory(story: TodayTitleStory): TodayStoryGroup {
            val friendFrames = story.actions.map { TodayStoryFrame.Friend(story, it) }
            val frames = when {
                story.users.size < 2 -> friendFrames
                else -> persistentListOf(TodayStoryFrame.Summary(story)) + friendFrames
            }

            return Title(
                story = story,
                frames = frames.toImmutableList(),
            )
        }
    }
}
